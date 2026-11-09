-- Earn Money initial schema.
-- Apply through the Supabase SQL editor or a controlled migration process.
-- No demo task or real-money credit is seeded by this migration.

create extension if not exists pgcrypto;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text not null default '',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.profiles (id, display_name)
  values (
    new.id,
    coalesce(new.raw_user_meta_data ->> 'display_name', '')
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();

create table if not exists public.tasks (
  id uuid primary key default gen_random_uuid(),
  provider text not null,
  provider_task_id text not null,
  title text not null,
  description text not null default '',
  category text not null default 'general',
  terms_url text,
  is_active boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (provider, provider_task_id)
);

create table if not exists public.wallet_ledger (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete restrict,
  provider text,
  provider_transaction_id text,
  entry_type text not null check (entry_type in ('reward_pending', 'reward_release', 'withdrawal', 'reversal', 'adjustment')),
  bucket text not null check (bucket in ('pending', 'available')),
  amount numeric(14,2) not null check (amount <> 0),
  currency text not null default 'EGP' check (currency = 'EGP'),
  description text not null default '',
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);

create unique index if not exists wallet_ledger_provider_tx_unique
  on public.wallet_ledger(provider, provider_transaction_id, entry_type)
  where provider is not null and provider_transaction_id is not null;

create index if not exists wallet_ledger_user_created_idx
  on public.wallet_ledger(user_id, created_at desc);

create or replace view public.wallet_balances
with (security_invoker = true)
as
select
  user_id,
  coalesce(sum(amount) filter (where bucket = 'pending'), 0)::numeric(14,2) as pending_amount,
  coalesce(sum(amount) filter (where bucket = 'available'), 0)::numeric(14,2) as available_amount
from public.wallet_ledger
group by user_id;

create table if not exists public.withdrawal_requests (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete restrict,
  amount numeric(14,2) not null check (amount > 0),
  currency text not null default 'EGP' check (currency = 'EGP'),
  payout_method text not null,
  payout_details_encrypted text,
  status text not null default 'requested'
    check (status in ('requested', 'reviewing', 'approved', 'paid', 'rejected', 'cancelled')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

alter table public.profiles enable row level security;
alter table public.tasks enable row level security;
alter table public.wallet_ledger enable row level security;
alter table public.withdrawal_requests enable row level security;

drop policy if exists "Users can read own profile" on public.profiles;
create policy "Users can read own profile"
  on public.profiles for select to authenticated
  using (auth.uid() = id);

drop policy if exists "Users can update own profile" on public.profiles;
create policy "Users can update own profile"
  on public.profiles for update to authenticated
  using (auth.uid() = id)
  with check (auth.uid() = id);

drop policy if exists "Authenticated users can read active tasks" on public.tasks;
create policy "Authenticated users can read active tasks"
  on public.tasks for select to authenticated
  using (is_active = true);

-- Ledger and withdrawal rows are accessed only through the server API.
-- Do not add client-side insert/update policies for money-related tables.
