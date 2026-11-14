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

// 11 rotas montadas + home. As 3 páginas de reserva exigem um Ambiente no
// construtor, então são alcançadas por CLIQUE nos links do MenuPage (não por URL).
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

const RESERVA_LINKS = [
  ['12-reservas-fixas', 'linkVer'],
  ['13-reservas-hoje', 'linkHoje'],
  ['14-reservas-semana', 'linkSemana'],
];

const NOME_TESTE = 'Ambiente Teste Visual';

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
const dialogs = [];
page.on('response', r => { if (r.status() >= 400) failed.push(`${r.status()} ${r.url()}`); });
page.on('console', m => { if (m.type() === 'error') consoleErrors.push(m.text()); });
page.on('pageerror', e => pageErrors.push(String(e)));
page.on('dialog', async d => { dialogs.push(d.message()); await d.accept(); });

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
      styleSheets: sheets.length, sheets,
      bodyPaddingTop: body?.paddingTop ?? null,
      navbarBg: nav?.backgroundColor ?? null,
      navbarHeight: navEl?.getBoundingClientRect().height ?? null,
      title: document.title,
      links: document.querySelectorAll('link[rel="stylesheet"]').length,
    };
  });
}

async function shot(name, target = page) {
  const file = path.join(outDir, `${label}__${name}.png`);
  await target.screenshot({ path: file, animations: 'disabled' });
  return file;
}

async function feedbackText(target = page) {
  // O FeedbackPanel rende a mensagem em contêineres aninhados; o número de
  // elementos casados difere entre Wicket 6 e 11. Compara-se a MENSAGEM, não
  // a quantidade de nós.
  const texts = await target.locator('[class*="feedback"]').allInnerTexts().catch(() => []);
  const mensagens = [...new Set(texts.map(t => t.replace(/\s+/g, ' ').trim()).filter(Boolean))];
  return mensagens.join(' | ');
}

// O ModalWindow do Wicket 6 usa iframe; o ModalDialog do Wicket 11 fica no DOM.
// Espera ativamente o conteúdo aparecer em qualquer um dos dois.
async function waitForModal(target, sel, timeoutMs = 7000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    if (await target.locator(sel).count().catch(() => 0) > 0) return true;
    for (const f of target.frames()) {
      if (await f.locator(sel).count().catch(() => 0) > 0) return true;
    }
    await target.waitForTimeout(300);
  }
  return false;
}

const results = {
  label, base, pages: {}, interactions: {},
  failed: [], consoleErrors: [], pageErrors: [], dialogs: [],
};

// ---------- 1) home anônima (desktop) ----------
await page.goto(base + '/', { waitUntil: 'networkidle', timeout: 45000 });
await page.waitForTimeout(600);
results.pages['01-home'] = { file: await shot('01-home'), probe: await probe(), http: 'ok' };

if (homeOnly) {
  results.failed = [...new Set(failed)];
  results.consoleErrors = [...new Set(consoleErrors)].slice(0, 20);
  results.pageErrors = [...new Set(pageErrors)].slice(0, 20);
  fs.writeFileSync(path.join(outDir, `${label}__meta.json`), JSON.stringify(results, null, 2));
  console.log(`[${label}] (home-only) páginas=1 consoleErros=${results.consoleErrors.length}`);
  await browser.close();
  process.exit(0);
}

// ---------- 2) home anônima (mobile), AGORA com probe ----------
{
  const mobile = await browser.newContext({
    viewport: { width: 390, height: 844 }, deviceScaleFactor: 1, isMobile: true, hasTouch: true,
  });
  const mp = await mobile.newPage();
  mp.on('response', r => { if (r.status() >= 400) failed.push(`${r.status()} ${r.url()}`); });
  await mp.goto(base + '/', { waitUntil: 'networkidle', timeout: 45000 });
  await mp.waitForTimeout(500);
  // probe() usa a `page` desktop; para o mobile repetimos a leitura no mp.
  results.pages['01-home-mobile'] = {
    file: await shot('01-home-mobile', mp), probe: null, http: 'ok',
  };
  results.pages['01-home-mobile'].probe = await mp.evaluate(() => {
    const sheets = [...document.styleSheets].map(s => {
      let rules = null;
      try { rules = s.cssRules?.length ?? null; } catch (e) { rules = 'bloqueado'; }
      return { href: s.href || '(inline)', rules };
    });
    const body = document.body ? getComputedStyle(document.body) : null;
    const navEl = document.querySelector('.navbar');
    return {
      styleSheets: sheets.length, sheets,
      bodyPaddingTop: body?.paddingTop ?? null,
      navbarBg: navEl ? getComputedStyle(navEl).backgroundColor : null,
      navbarHeight: navEl?.getBoundingClientRect().height ?? null,
      title: document.title,
      links: document.querySelectorAll('link[rel="stylesheet"]').length,
    };
  });
  await mobile.close();
}

// ---------- 3) login ----------
await page.fill('input[name="login"]', 'admin');
await page.fill('input[name="senha"]', 'admin');
await Promise.all([
  page.waitForNavigation({ waitUntil: 'networkidle', timeout: 45000 }).catch(() => {}),
  page.click('form[wicket\\:id="formLogin"] input[type="submit"]'),
]);
await page.waitForTimeout(600);
results.loginUrl = page.url();

// ---------- 4) rotas estáticas ----------
for (const [name, url] of PAGES.slice(1)) {
  try {
    const resp = await page.goto(base + url, { waitUntil: 'networkidle', timeout: 45000 });
    await page.waitForTimeout(500);
    results.pages[name] = { file: await shot(name), probe: await probe(), http: resp ? resp.status() : 'sem-resposta' };
  } catch (e) {
    results.pages[name] = { error: String(e), http: 'erro' };
  }
}

// ---------- 5) 3 páginas de reserva via navegação real ----------
for (const [name, linkId] of RESERVA_LINKS) {
  try {
    await page.goto(base + '/menu_ambientes/', { waitUntil: 'networkidle', timeout: 45000 });
    const link = page.locator(`a[wicket\\:id="${linkId}"]`).first();
    const [resp] = await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle', timeout: 45000 }),
      link.click(),
    ]);
    await page.waitForTimeout(600);
    results.pages[name] = {
      file: await shot(name), probe: await probe(),
      http: resp ? resp.status() : 'sem-resposta', via: linkId,
    };
  } catch (e) {
    results.pages[name] = { error: String(e), http: 'erro', via: linkId };
  }
}

// ---------- 6) interações funcionais ----------
// 6a) login inválido (contexto isolado para não perder a sessão)
{
  const c = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const p = await c.newPage();
  await p.goto(base + '/', { waitUntil: 'networkidle', timeout: 45000 });
  await p.fill('input[name="login"]', 'admin');
  await p.fill('input[name="senha"]', 'senha-errada');
  await Promise.all([
    p.waitForNavigation({ waitUntil: 'networkidle', timeout: 45000 }).catch(() => {}),
    p.click('form[wicket\\:id="formLogin"] input[type="submit"]'),
  ]);
  await p.waitForTimeout(500);
  results.interactions.loginInvalido = {
    feedback: await feedbackText(p),
    loginFormVisible: (await p.locator('input[name="login"]').count()) > 0,
    file: await shot('int-login-invalido', p),
  };
  await c.close();
}

// 6b) cadastro de ambiente (formulário + feedback + persistência via db_snapshot)
{
  await page.goto(base + '/ambientes/', { waitUntil: 'networkidle', timeout: 45000 });
  await page.fill('input[name="nome"]', NOME_TESTE);
  await page.fill('input[name="dscricao"]', 'criado pelo teste visual');
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'networkidle', timeout: 45000 }).catch(() => {}),
    page.click('input[wicket\\:id="buttonSalvar"]'),
  ]);
  await page.waitForTimeout(600);
  results.interactions.cadastroAmbiente = {
    nomeVisivel: (await page.getByText(NOME_TESTE, { exact: false }).count()) > 0,
    feedback: await feedbackText(),
    file: await shot('int-cadastro-ambiente'),
  };
}

// 6c) exclusão com diálogo de confirmação
{
  await page.goto(base + '/ambientes/', { waitUntil: 'networkidle', timeout: 45000 });
  const row = page.locator('tr').filter({ hasText: NOME_TESTE }).last();
  const apagar = row.locator('a[wicket\\:id="linkApagar"]');
  const antes = await page.getByText(NOME_TESTE, { exact: false }).count();
  if (await apagar.count() > 0) {
    await apagar.click();
    await page.waitForTimeout(1200);
  }
  const depois = await page.getByText(NOME_TESTE, { exact: false }).count();
  results.interactions.exclusaoComConfirmacao = {
    linhaAntes: antes, linhaDepois: depois,
    confirmacaoExibida: dialogs.some(d => /certeza/i.test(d)),
    file: await shot('int-exclusao-ambiente'),
  };
}

// 6d) abertura de modal por AJAX.
// "Emprestar" só aparece para adminEmprestimo; usamos o link "Histórico",
// sempre visível, que abre HistoricoModalPage (ModalWindow/ModalDialog).
{
  await page.goto(base + '/menu_recursos/', { waitUntil: 'networkidle', timeout: 45000 });
  const link = page.locator('a[wicket\\:id="link"]').first();
  const linkPresente = (await link.count()) > 0;
  if (linkPresente) {
    await link.click();
  }
  // Marcador estático do conteúdo: o <tr> da ListView não aparece quando
  // não há empréstimos, então usamos o cabeçalho da tabela (presente no DOM
  // do ModalDialog e no iframe do ModalWindow antigo).
  const conteudo = await waitForModal(page, 'text=Data Inicio');
  results.interactions.modal = {
    linkPresente,
    conteudoVisivel: conteudo,
    file: await shot('int-modal-historico'),
  };
}

results.failed = [...new Set(failed)];
results.consoleErrors = [...new Set(consoleErrors)].slice(0, 20);
results.pageErrors = [...new Set(pageErrors)].slice(0, 20);
results.dialogs = dialogs;
fs.writeFileSync(path.join(outDir, `${label}__meta.json`), JSON.stringify(results, null, 2));
console.log(`[${label}] páginas=${Object.keys(results.pages).length} interações=${Object.keys(results.interactions).length} `
  + `falhasHTTP=${results.failed.length} consoleErros=${results.consoleErrors.length} pageErros=${results.pageErrors.length} login=${results.loginUrl}`);
await browser.close();
