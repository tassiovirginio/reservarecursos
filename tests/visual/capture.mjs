import { chromium } from 'playwright-core';
import fs from 'node:fs';
import path from 'node:path';

// Uso: node capture.mjs <label> <outDir> <baseUrl> [--home-only]
const label = process.argv[2];
const outDir = process.argv[3];
const base = process.argv[4] || 'http://127.0.0.1:18081';
const homeOnly = process.argv.includes('--home-only');
const exe = process.env.VISUAL_CHROME_PATH;

if (!exe) {
  console.error('VISUAL_CHROME_PATH não definido (run.sh deve detectar o Chromium).');
  process.exit(2);
}

const PAGES = [
  ['01-home', '/'],
  ['02-menu-ambientes', '/menu_ambientes/'],
  ['03-ambientes', '/ambientes/'],
  ['04-recursos', '/recursos/'],
  ['05-horarios', '/horarios/'],
  ['06-periodos', '/periodos/'],
  ['07-usuarios', '/usuarios/'],
  ['08-grupos', '/grupos/'],
  ['09-relatorios', '/relatorios/'],
  ['10-gerenciar', '/gerenciar/'],
  ['11-menu-recursos', '/menu_recursos/'],
];

const browser = await chromium.launch({
  executablePath: exe,
  headless: true,
  args: ['--no-sandbox', '--disable-dev-shm-usage'],
});
const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 }, deviceScaleFactor: 1 });
const page = await ctx.newPage();

const failed = [];
const consoleErrors = [];
const pageErrors = [];
page.on('response', r => { if (r.status() >= 400) failed.push(`${r.status()} ${r.url()}`); });
page.on('console', m => { if (m.type() === 'error') consoleErrors.push(m.text()); });
page.on('pageerror', e => pageErrors.push(String(e)));

async function probe() {
  return await page.evaluate(() => {
    const sheets = [...document.styleSheets].map(s => {
      let rules = null;
      try { rules = s.cssRules?.length ?? null; } catch (e) { rules = 'bloqueado'; }
      return { href: s.href || '(inline)', rules };
    });
    const body = document.body ? getComputedStyle(document.body) : null;
    const navEl = document.querySelector('.navbar');
    const nav = navEl ? getComputedStyle(navEl) : null;
    return {
      styleSheets: sheets.length,
      sheets,
      bodyPaddingTop: body?.paddingTop ?? null,
      navbarBg: nav?.backgroundColor ?? null,
      navbarHeight: navEl?.getBoundingClientRect().height ?? null,
      title: document.title,
      links: document.querySelectorAll('link[rel="stylesheet"]').length,
    };
  });
}

async function shot(name) {
  const file = path.join(outDir, `${label}__${name}.png`);
  await page.screenshot({ path: file, animations: 'disabled' });
  return file;
}

const results = { label, base, pages: {}, failed: [], consoleErrors: [], pageErrors: [] };

await page.goto(base + '/', { waitUntil: 'networkidle', timeout: 45000 });
await page.waitForTimeout(600);
results.pages['01-home'] = { file: await shot('01-home'), probe: await probe(), http: 'ok' };

if (!homeOnly) {
  const mobile = await browser.newContext({
    viewport: { width: 390, height: 844 }, deviceScaleFactor: 1, isMobile: true, hasTouch: true,
  });
  const mp = await mobile.newPage();
  await mp.goto(base + '/', { waitUntil: 'networkidle', timeout: 45000 });
  await mp.waitForTimeout(500);
  await mp.screenshot({ path: path.join(outDir, `${label}__01-home-mobile.png`), animations: 'disabled' });
  results.pages['01-home-mobile'] = { file: `${label}__01-home-mobile.png`, probe: null, http: 'ok' };
  await mobile.close();

  await page.fill('input[name="login"]', 'admin');
  await page.fill('input[name="senha"]', 'admin');
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'networkidle', timeout: 45000 }).catch(() => {}),
    page.click('form[wicket\\:id="formLogin"] input[type="submit"]'),
  ]);
  await page.waitForTimeout(600);
  results.loginUrl = page.url();

  for (const [name, url] of PAGES.slice(1)) {
    try {
      const resp = await page.goto(base + url, { waitUntil: 'networkidle', timeout: 45000 });
      await page.waitForTimeout(500);
      results.pages[name] = { file: await shot(name), probe: await probe(), http: resp ? resp.status() : 'sem-resposta' };
    } catch (e) {
      results.pages[name] = { error: String(e), http: 'erro' };
    }
  }
}

results.failed = [...new Set(failed)];
results.consoleErrors = [...new Set(consoleErrors)].slice(0, 20);
results.pageErrors = [...new Set(pageErrors)].slice(0, 20);
fs.writeFileSync(path.join(outDir, `${label}__meta.json`), JSON.stringify(results, null, 2));
console.log(`[${label}] páginas=${Object.keys(results.pages).length} falhasHTTP=${results.failed.length} consoleErros=${results.consoleErrors.length} pageErros=${results.pageErrors.length} login=${results.loginUrl ?? 'n/a'}`);
await browser.close();
