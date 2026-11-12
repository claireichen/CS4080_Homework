#!/usr/bin/env bash
set -euo pipefail

chapter_dir="$(cd "$(dirname "$0")" && pwd)"
build_dir="$(mktemp -d "${TMPDIR:-/tmp}/clox-ip-benchmark.XXXXXX")"
trap 'rm -rf "$build_dir"' EXIT

compiler="${CC:-cc}"
runs="${RUNS:-5}"
common_flags=(-std=c99 -O2 -Wall -Wextra -Werror)

"$compiler" "${common_flags[@]}" -DNO_IP_CACHE "$chapter_dir"/*.c \
  -o "$build_dir/clox-baseline"
"$compiler" "${common_flags[@]}" "$chapter_dir"/*.c \
  -o "$build_dir/clox-cached-ip"

measure() {
  { /usr/bin/time -p "$1" "$2" >/dev/null; } 2>&1 |
    awk '$1 == "real" { print $2 }'
}

print_average() {
  local label="$1"
  local results="$2"
  printf "%s" "$results" | awk -v label="$label" '
    NF { total += $1; count++ }
    END { printf "  %-12s %.6f seconds average\n", label, total / count }
  '
}

for script in IpBenchmarkCalls.lox IpBenchmarkFib.lox; do
  echo "$script ($runs runs)"
  script_path="$chapter_dir/$script"
  baseline_results=""
  cached_results=""

  "$build_dir/clox-baseline" "$script_path" >/dev/null
  "$build_dir/clox-cached-ip" "$script_path" >/dev/null

  for ((run = 1; run <= runs; run++)); do
    if ((run % 2 == 1)); then
      baseline_results+="$(measure "$build_dir/clox-baseline" "$script_path")"$'\n'
      cached_results+="$(measure "$build_dir/clox-cached-ip" "$script_path")"$'\n'
    else
      cached_results+="$(measure "$build_dir/clox-cached-ip" "$script_path")"$'\n'
      baseline_results+="$(measure "$build_dir/clox-baseline" "$script_path")"$'\n'
    fi
  done

  print_average "baseline" "$baseline_results"
  print_average "cached ip" "$cached_results"
done
