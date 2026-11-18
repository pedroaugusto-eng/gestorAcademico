/*
 * Cliente da API. Todas as telas usam estas funções.
 * - A sessão fica no cookie JSESSIONID (HttpOnly), o JavaScript nunca toca nele.
 * - Para POST/PUT/DELETE, envia o token CSRF lido do cookie XSRF-TOKEN no cabeçalho X-XSRF-TOKEN.
 * - Erros da API chegam no formato Problem Details: a mensagem está em "detail".
 */
const Api = (() => {
  function lerCookie(nome) {
    const par = document.cookie.split('; ').find((item) => item.startsWith(nome + '='));
    return par ? decodeURIComponent(par.substring(nome.length + 1)) : null;
  }

  async function garantirTokenCsrf() {
    if (!lerCookie('XSRF-TOKEN')) {
      await fetch('/api/auth/csrf', { credentials: 'same-origin' });
    }
  }

  async function requisitar(metodo, url, { json, formulario } = {}) {
    const cabecalhos = { Accept: 'application/json' };
    let corpo;

    if (metodo !== 'GET') {
      await garantirTokenCsrf();
      cabecalhos['X-XSRF-TOKEN'] = lerCookie('XSRF-TOKEN') || '';
    }
    if (json !== undefined) {
      cabecalhos['Content-Type'] = 'application/json';
      corpo = JSON.stringify(json);
    }
    if (formulario) {
      corpo = new URLSearchParams(formulario);
    }

    let resposta;
    try {
      resposta = await fetch(url, { method: metodo, headers: cabecalhos, body: corpo, credentials: 'same-origin' });
    } catch (falhaDeRede) {
      const erro = new Error('Sem conexão com o servidor. Confira se a aplicação está rodando.');
      erro.status = 0;
      erro.campos = {};
      throw erro;
    }

    if (resposta.status === 204) {
      return null;
    }
    const tipo = resposta.headers.get('content-type') || '';
    const dados = tipo.includes('json') ? await resposta.json() : null;

    if (!resposta.ok) {
      const erro = new Error((dados && dados.detail) || 'Não foi possível concluir. Tente de novo.');
      erro.status = resposta.status;
      erro.campos = (dados && dados.campos) || {};
      throw erro;
    }
    return dados;
  }

  return {
    entrar: (identificador, senha) => requisitar('POST', '/api/auth/login', { formulario: { identificador, senha } }),
    sair: () => requisitar('POST', '/api/auth/logout'),
    cadastrar: (aluno) => requisitar('POST', '/api/alunos', { json: aluno }),
    eu: () => requisitar('GET', '/api/alunos/me'),
    disciplinas: () => requisitar('GET', '/api/disciplinas'),
    matriculas: () => requisitar('GET', '/api/matriculas'),
    matricular: (disciplinaId) => requisitar('POST', '/api/matriculas', { json: { disciplinaId } }),
    lancarNota: (matriculaId, nota, tipo, unidade) =>
      requisitar('POST', `/api/matriculas/${encodeURIComponent(matriculaId)}/avaliacoes`, { json: { nota, tipo, unidade } }),
    alterarNota: (matriculaId, avaliacaoId, nota, tipo, unidade) =>
      requisitar('PUT', `/api/matriculas/${encodeURIComponent(matriculaId)}/avaliacoes/${encodeURIComponent(avaliacaoId)}`,
        { json: { nota, tipo, unidade } }),
    desfazerFalta: (matriculaId) =>
      requisitar('DELETE', `/api/matriculas/${encodeURIComponent(matriculaId)}/faltas`),
    excluirNota: (matriculaId, avaliacaoId) =>
      requisitar('DELETE', `/api/matriculas/${encodeURIComponent(matriculaId)}/avaliacoes/${encodeURIComponent(avaliacaoId)}`),
    registrarFalta: (matriculaId) =>
      requisitar('POST', `/api/matriculas/${encodeURIComponent(matriculaId)}/faltas`),
  };
})();

/* Utilidades de tela compartilhadas. */
const Tela = (() => {
  let temporizador;

  function avisar(mensagem, tipo = 'sucesso') {
    const caixa = document.getElementById('aviso');
    if (!caixa) return;
    caixa.textContent = mensagem;
    caixa.className = 'aviso aviso--' + tipo + ' aviso--visivel';
    clearTimeout(temporizador);
    temporizador = setTimeout(() => caixa.classList.remove('aviso--visivel'), 4500);
  }

  function numero(valor) {
    return Number(valor).toLocaleString('pt-BR', { maximumFractionDigits: 2 });
  }

  /* Cria elementos com textContent: dado vindo do servidor nunca vira HTML (evita XSS). */
  function el(tag, classe, texto) {
    const elemento = document.createElement(tag);
    if (classe) elemento.className = classe;
    if (texto !== undefined && texto !== null) elemento.textContent = texto;
    return elemento;
  }

  function mostrarErrosDeCampo(formulario, campos) {
    formulario.querySelectorAll('.campo__erro').forEach((erro) => { erro.textContent = ''; });
    formulario.querySelectorAll('[aria-invalid]').forEach((input) => input.removeAttribute('aria-invalid'));
    Object.entries(campos || {}).forEach(([nome, mensagem]) => {
      const input = formulario.querySelector(`[name="${nome}"]`);
      const erro = formulario.querySelector(`[data-erro-de="${nome}"]`);
      if (input) input.setAttribute('aria-invalid', 'true');
      if (erro) erro.textContent = mensagem;
    });
  }

  function ocupado(botao, sim) {
    botao.disabled = sim;
    botao.classList.toggle('botao--ocupado', sim);
  }

  return { avisar, numero, el, mostrarErrosDeCampo, ocupado };
})();
