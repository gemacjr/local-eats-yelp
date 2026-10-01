#!/usr/bin/env bash
# End-to-end check of every feature through the public entry point (default: the Next.js app).
# Usage: scripts/smoke-test.sh [base_url]
set -euo pipefail
BASE="${1:-http://localhost:3000}"
EMAIL="smoke-$(date +%s)-$RANDOM@example.com"
pass() { printf '  \033[32m✔\033[0m %s\n' "$1"; }
fail() { printf '  \033[31m✘\033[0m %s\n' "$1"; exit 1; }
json() { python3 -c "import sys,json; d=json.load(sys.stdin); print(eval(sys.argv[1], {}, {'d': d}))" "$1"; }

echo "Smoke testing $BASE"

[[ $(curl -s "$BASE/api/locations" | json 'len(d)') -gt 0 ]] && pass "GET /api/locations" || fail "locations"
[[ $(curl -s "$BASE/api/categories" | json '"Pizza" in d') == True ]] && pass "GET /api/categories" || fail "categories"

r=$(curl -s "$BASE/api/businesses?query=burrito")
[[ $(echo "$r" | json 'd["items"][0]["name"]') == "La Taqueria" ]] && pass "text search: burrito -> La Taqueria" || fail "text search: $r"

r=$(curl -s "$BASE/api/businesses?lat=37.7614&lng=-122.4241&radius=1000")
[[ $(echo "$r" | json 'd["items"][0]["name"]') == "Tartine Bakery" ]] && pass "geo search: nearest to Tartine is Tartine" || fail "geo search: $r"

r=$(curl -s "$BASE/api/businesses?location=sf-north-beach&category=Coffee")
[[ $(echo "$r" | json '[b["name"] for b in d["items"]]') == "['Caffe Trieste']" ]] && pass "named-area + category search" || fail "area search: $r"

TOKEN=$(curl -s -X POST "$BASE/api/auth/register" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"displayName\":\"Smoke\",\"password\":\"password123\"}" | json 'd["token"]')
[[ -n "$TOKEN" ]] && pass "register" || fail "register"
TOKEN=$(curl -s -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"password123\"}" | json 'd["token"]')
AUTH=(-H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json')
[[ $(curl -s "$BASE/api/auth/me" "${AUTH[@]}" | json 'd["email"]') == "$EMAIL" ]] && pass "login + /me" || fail "login"

BID=$(curl -s -X POST "$BASE/api/businesses" "${AUTH[@]}" \
  -d '{"name":"Smoke Test Cafe","category":"Coffee","address":"3000 24th St, San Francisco","latitude":37.7527,"longitude":-122.4125}' | json 'd["id"]')
[[ $(curl -s "$BASE/api/businesses?location=sf-mission&query=smoke" | json "any(b['id']==$BID for b in d['items'])") == True ]] \
  && pass "create business (auto-tagged into Mission District)" || fail "create business"

code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/api/businesses/$BID/reviews" "${AUTH[@]}" -d '{"rating":4,"text":"Solid espresso"}')
[[ $code == 201 ]] && pass "post review" || fail "post review ($code)"
code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/api/businesses/$BID/reviews" "${AUTH[@]}" -d '{"rating":1}')
[[ $code == 409 ]] && pass "duplicate review rejected (409)" || fail "duplicate review ($code)"
r=$(curl -s "$BASE/api/businesses/$BID")
[[ $(echo "$r" | json '(d["avgRating"], d["numRatings"])') == "(4.0, 1)" ]] && pass "rating aggregate updated" || fail "aggregate: $r"

UP=$(curl -s -X POST "$BASE/api/businesses/$BID/photos/upload-url" "${AUTH[@]}" -d '{"contentType":"image/png"}')
URL=$(echo "$UP" | json 'd["uploadUrl"]'); KEY=$(echo "$UP" | json 'd["key"]')
printf '\x89PNG\r\n\x1a\n' > /tmp/yelp-smoke.png
code=$(curl -s -o /dev/null -w '%{http_code}' -X PUT "$URL" -H 'Content-Type: image/png' --data-binary @/tmp/yelp-smoke.png)
[[ $code == 200 ]] && pass "direct upload to S3 via presigned URL" || fail "S3 PUT ($code) $URL"
code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/api/businesses/$BID/photos" "${AUTH[@]}" -d "{\"key\":\"$KEY\"}")
[[ $code == 201 ]] && pass "confirm photo" || fail "confirm photo ($code)"
PHOTO=$(curl -s "$BASE/api/businesses/$BID/photos" | json 'd[0]["url"]')
[[ $(curl -s -o /dev/null -w '%{http_code}' "$PHOTO") == 200 ]] && pass "presigned GET serves the photo" || fail "photo GET"

code=$(curl -s -o /dev/null -w '%{http_code}' "$BASE/businesses/$BID")
[[ $code == 200 ]] && pass "SSR business page renders" || fail "business page ($code)"
echo "All checks passed."
