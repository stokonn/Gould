// Readiness shell only. Enabling a variable cannot activate unverified API code.
// No network, database, cookies, token parsing, logging, static assets or OAuth.
export default {
  async fetch() {
    return new Response('{"error":"private_staging_disabled"}', {
      status: 503,
      headers: {
        'Content-Type': 'application/json; charset=utf-8',
        'Cache-Control': 'no-store',
        'Content-Security-Policy': "default-src 'none'; frame-ancestors 'none'",
        'X-Content-Type-Options': 'nosniff',
        'Referrer-Policy': 'no-referrer',
      },
    });
  },
};
