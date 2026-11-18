/*
 * Portal do aluno. Três telas, escolhidas pelo endereço:
 *   #minhas            lista curta das disciplinas matriculadas
 *   #catalogo          todas as disciplinas, com o botão de matrícula
 *   #disciplina/{id}   detalhe de uma matrícula: notas por unidade, média, faltas e lançamentos
 * Assim o botão "voltar" do navegador funciona e dá para compartilhar o link de uma tela.
 *
 * Regras (calculadas no servidor; a tela só apresenta):
 *   - 2 unidades, cada uma de 0 a 10 pontos; média = (U1 + U2) / 2; aprovação com média 6.
 *   - cada falta conta 2 aulas; pode passar do limite e aí fica "Reprovado por falta",
 *     que vale mesmo se a média tiver sido atingida.
 */
(() => {
  const NOMES_DOS_TIPOS = { PROVA: 'Prova', ATIVIDADE: 'Atividade' };
  const NOMES_DAS_SECOES = { minhas: 'Minhas disciplinas', catalogo: 'Catálogo', detalhe: 'Disciplina' };
  const AULAS_POR_FALTA = 2;

  const $ = (id) => document.getElementById(id);
  const vistas = { minhas: $('vista-minhas'), catalogo: $('vista-catalogo'), detalhe: $('vista-detalhe') };
  const estado = { disciplinas: [], matriculas: [], detalheAtual: null };
  let primeiraVez = true;

  /* ---------- Utilidades ---------- */

  /*
   * O banco guarda os nomes em caixa alta ("PROGRAMAÇÃO ORIENTADA A OBJETOS").
   * Na tela eles aparecem em caixa mista, mais fáceis de ler. Nomes que já vêm
   * em caixa mista ficam como estão; algarismos romanos continuam em maiúsculas.
   */
  const PALAVRAS_MINUSCULAS = new Set(['a', 'à', 'ao', 'aos', 'as', 'com', 'da', 'das', 'de', 'do', 'dos',
    'e', 'em', 'na', 'nas', 'no', 'nos', 'o', 'os', 'para', 'por']);
  function nomeLegivel(nome) {
    if (!nome || nome !== nome.toLocaleUpperCase('pt-BR')) return nome;
    return nome.toLocaleLowerCase('pt-BR').split(/\s+/).map((palavra, i) => {
      if (/^[ivxlc]{1,4}$/.test(palavra)) return palavra.toLocaleUpperCase('pt-BR');
      if (i > 0 && PALAVRAS_MINUSCULAS.has(palavra)) return palavra;
      return palavra.charAt(0).toLocaleUpperCase('pt-BR') + palavra.slice(1);
    }).join(' ');
  }

  const n = (valor) => Tela.numero(valor);
  const plural = (qtd, singular, pluralTexto) => `${qtd} ${qtd === 1 ? singular : pluralTexto}`;

  function meta(d) {
    return `${d.codigo} · ${d.cargaHoraria}h · ${d.semestreRecomendado}º semestre`;
  }

  function perto(m) {
    return m.limiteDeFaltas > 0 && m.faltas >= m.limiteDeFaltas * 0.75;
  }

  /* Ordem de importância: reprovação por falta vence a média. */
  function situacao(m) {
    if (m.reprovadoPorFalta) return { texto: 'Reprovado por falta', classe: 'selo--reprovado' };
    if (m.mediaAtingida) return { texto: 'Média atingida', classe: 'selo--ok' };
    if (perto(m)) return { texto: 'Atenção às faltas', classe: 'selo--alerta' };
    return { texto: 'Em andamento', classe: 'selo--neutro' };
  }

  function listaDeDados(pares) {
    const dl = Tela.el('dl', 'dados');
    pares.forEach(([termo, valor]) => {
      const linha = Tela.el('div', 'dados__linha');
      linha.append(Tela.el('dt', null, termo), Tela.el('dd', null, valor));
      dl.append(linha);
    });
    return dl;
  }

  function botao(texto, classes, rotuloAcessivel) {
    const b = Tela.el('button', 'botao ' + classes, texto);
    b.type = 'button';
    if (rotuloAcessivel) b.setAttribute('aria-label', rotuloAcessivel);
    return b;
  }

  function seletor(nome, opcoes, selecionado, rotulo) {
    const select = document.createElement('select');
    select.name = nome;
    select.setAttribute('aria-label', rotulo);
    opcoes.forEach(([valor, texto]) => select.append(new Option(texto, valor, false, String(valor) === String(selecionado))));
    return select;
  }

  const OPCOES_UNIDADE = [[1, '1ª unidade'], [2, '2ª unidade']];
  const OPCOES_TIPO = Object.entries(NOMES_DOS_TIPOS);

  /* Aceita "7,5" ou "7.5"; avisa e devolve null se estiver fora de 0 a 10. */
  function lerNota(campo) {
    const texto = String(campo.value).trim().replace(',', '.');
    const nota = Number(texto);
    if (texto === '' || Number.isNaN(nota) || nota < 0 || nota > 10) {
      Tela.avisar('Digite uma nota de 0 a 10.', 'erro');
      campo.focus();
      return null;
    }
    return nota;
  }

  /* Se a sessão expirou, volta para a tela de entrada. */
  function tratarErro(falha) {
    if (falha.status === 401) {
      window.location.replace('/');
      return;
    }
    Tela.avisar(falha.message, 'erro');
  }

  function guardarMatricula(atualizada) {
    const existe = estado.matriculas.some((m) => m.id === atualizada.id);
    estado.matriculas = existe
      ? estado.matriculas.map((m) => (m.id === atualizada.id ? atualizada : m))
      : [...estado.matriculas, atualizada];
  }

  function matriculaAtual() {
    return estado.matriculas.find((m) => m.id === estado.detalheAtual);
  }

  /* ---------- Rotas ---------- */

  function lerRota() {
    const endereco = decodeURIComponent(window.location.hash.replace(/^#/, ''));
    const detalhe = /^disciplina\/(\d+)$/.exec(endereco);
    if (detalhe) return { secao: 'detalhe', id: Number(detalhe[1]) };
    if (endereco === 'minhas' || endereco === 'catalogo') return { secao: endereco };
    // Sem endereço: quem ainda não tem matrícula começa pelo catálogo.
    return { secao: estado.matriculas.length ? 'minhas' : 'catalogo' };
  }

  function renderizar() {
    let rota = lerRota();
    if (rota.secao === 'detalhe' && !estado.matriculas.some((m) => m.id === rota.id)) {
      Tela.avisar('Essa disciplina não está entre as suas matrículas.', 'erro');
      history.replaceState(null, '', '#minhas');
      rota = { secao: 'minhas' };
    }

    Object.entries(vistas).forEach(([nome, vista]) => { vista.hidden = nome !== rota.secao; });
    const secaoDoMenu = rota.secao === 'detalhe' ? 'minhas' : rota.secao;
    document.querySelectorAll('[data-secao]').forEach((link) => {
      if (link.dataset.secao === secaoDoMenu) link.setAttribute('aria-current', 'page');
      else link.removeAttribute('aria-current');
    });

    if (rota.secao === 'minhas') desenharMinhas();
    if (rota.secao === 'catalogo') desenharCatalogo();
    if (rota.secao === 'detalhe') {
      esconderConfirmacaoDeFalta();
      desenharDetalhe(rota.id);
    }

    document.title = `Gestor Acadêmico · ${rota.secao === 'detalhe'
      ? $('titulo-detalhe').textContent : NOMES_DAS_SECOES[rota.secao]}`;

    if (!primeiraVez) {
      window.scrollTo(0, 0);
      vistas[rota.secao].querySelector('h1').focus();
    }
    primeiraVez = false;
  }

  /* ---------- Minhas disciplinas ---------- */

  function desenharResumo() {
    const resumo = $('resumo');
    resumo.replaceChildren();
    if (!estado.matriculas.length) return;
    const avaliacoes = estado.matriculas.reduce((total, m) => total + m.avaliacoes.length, 0);
    [
      ['Disciplinas', estado.matriculas.length],
      ['Avaliações lançadas', avaliacoes],
      ['Com média atingida', estado.matriculas.filter((m) => m.mediaAtingida && !m.reprovadoPorFalta).length],
      ['Atenção às faltas', estado.matriculas.filter((m) => perto(m) || m.reprovadoPorFalta).length],
    ].forEach(([rotulo, valor]) => {
      const item = Tela.el('div', 'resumo__item');
      item.append(Tela.el('span', 'resumo__valor', String(valor)), Tela.el('span', 'resumo__rotulo', rotulo));
      resumo.append(item);
    });
  }

  function desenharMinhas() {
    desenharResumo();
    const lista = $('lista-minhas');
    lista.replaceChildren();

    if (!estado.matriculas.length) {
      const vazio = Tela.el('li', 'vazio');
      const irAoCatalogo = Tela.el('a', 'botao botao--primario', 'Ver catálogo de disciplinas');
      irAoCatalogo.href = '#catalogo';
      vazio.append(
        Tela.el('p', 'vazio__titulo', 'Você ainda não está em nenhuma disciplina.'),
        Tela.el('p', 'vazio__texto', 'Escolha no catálogo as disciplinas do semestre e matricule-se.'),
        irAoCatalogo,
      );
      lista.append(vazio);
      return;
    }

    estado.matriculas.forEach((m) => {
      const nome = nomeLegivel(m.disciplina.nome);
      const s = situacao(m);
      const item = Tela.el('li', 'item-disciplina');
      const texto = Tela.el('div', 'item-disciplina__texto');
      texto.append(Tela.el('h2', 'item-disciplina__nome', nome), Tela.el('p', 'item-disciplina__meta', meta(m.disciplina)));
      const ver = Tela.el('a', 'botao botao--secundario', 'Ver detalhes');
      ver.href = `#disciplina/${m.id}`;
      ver.setAttribute('aria-label', `Ver detalhes de ${nome}`);
      item.append(texto, Tela.el('span', `selo ${s.classe}`, s.texto), ver);
      lista.append(item);
    });
  }

  /* ---------- Catálogo ---------- */

  function desenharCatalogo() {
    const alvo = $('catalogo');
    alvo.replaceChildren();
    if (!estado.disciplinas.length) {
      alvo.append(Tela.el('p', 'vazio__texto', 'Nenhuma disciplina disponível no momento.'));
      return;
    }
    // O servidor já devolve em ordem de semestre recomendado e nome.
    const grade = Tela.el('div', 'catalogo__grade');
    estado.disciplinas.forEach((d) => grade.append(itemDoCatalogo(d)));
    alvo.append(grade);
  }

  function itemDoCatalogo(d) {
    const nome = nomeLegivel(d.nome);
    const cartao = Tela.el('article', 'cartao catalogo-item');
    cartao.append(
      Tela.el('p', 'catalogo-item__codigo', `${d.semestreRecomendado}º semestre · ${d.codigo}`),
      Tela.el('h2', 'catalogo-item__nome', nome),
    );
    if (d.curso) cartao.append(Tela.el('p', 'catalogo-item__curso', d.curso));
    cartao.append(listaDeDados([
      ['Carga horária', `${d.cargaHoraria}h`],
      ['Aulas no semestre', `${d.totalDeAulas} aulas de 75 min`],
      ['Pode faltar até', `${d.limiteDeFaltas} aulas`],
    ]));

    const rodape = Tela.el('div', 'catalogo-item__rodape');
    const matricula = estado.matriculas.find((m) => m.disciplina.id === d.id);
    if (matricula) {
      const ver = Tela.el('a', 'link', 'Ver detalhes');
      ver.href = `#disciplina/${matricula.id}`;
      ver.setAttribute('aria-label', `Ver detalhes de ${nome}`);
      rodape.append(Tela.el('span', 'selo selo--ok', 'Matriculado'), ver);
    } else {
      const matricular = botao('Matricular-se', 'botao--primario botao--largo', `Matricular-se em ${nome}`);
      matricular.addEventListener('click', () => matricularEm(d, matricular));
      rodape.append(matricular);
    }
    cartao.append(rodape);
    return cartao;
  }

  async function matricularEm(d, botaoMatricular) {
    Tela.ocupado(botaoMatricular, true);
    botaoMatricular.textContent = 'Matriculando…';
    try {
      const nova = await Api.matricular(d.id);
      guardarMatricula(nova);
      desenharCatalogo();
      Tela.avisar(`Matrícula feita em ${nomeLegivel(d.nome)}.`);
      const ver = $('catalogo').querySelector(`a[href="#disciplina/${nova.id}"]`);
      if (ver) ver.focus();
    } catch (falha) {
      tratarErro(falha);
      botaoMatricular.textContent = 'Matricular-se';
      Tela.ocupado(botaoMatricular, false);
    }
  }

  /* ---------- Detalhe ---------- */

  function desenharDetalhe(id) {
    const m = estado.matriculas.find((x) => x.id === id);
    const d = m.disciplina;
    const s = situacao(m);
    estado.detalheAtual = id;

    $('titulo-detalhe').textContent = nomeLegivel(d.nome);
    const selo = $('detalhe-selo');
    selo.className = `selo ${s.classe}`;
    selo.textContent = s.texto;
    $('detalhe-meta').textContent = [d.codigo, d.curso, `${d.cargaHoraria}h`, `${d.semestreRecomendado}º semestre`]
      .filter(Boolean).join(' · ');

    desenharMedia(m);
    desenharFaltas(m);

    $('detalhe-dados').replaceChildren(...listaDeDados([
      ['1ª unidade', `${n(m.unidades[0].nota)} de 10`],
      ['2ª unidade', `${n(m.unidades[1].nota)} de 10`],
      ['Carga horária', `${d.cargaHoraria}h`],
      ['Pode faltar até', `${d.limiteDeFaltas} aulas`],
    ]).children);

    desenharUnidades(m);
  }

  /* Média de 0 a 10, com a marca da média mínima (6) sobre a barra. */
  function desenharMedia(m) {
    const [u1, u2] = m.unidades;
    const parcial = u1.avaliacoes === 0 || u2.avaliacoes === 0;
    $('detalhe-media-rotulo').textContent = parcial && (u1.avaliacoes || u2.avaliacoes) ? 'Média parcial' : 'Média';
    $('detalhe-media').textContent = `${n(m.media)} de 10`;

    // Cinza abaixo da média mínima; verde a partir dela (a cor e o preenchimento mudam com transição).
    const barra = $('detalhe-barra-media');
    barra.style.transform = `scaleX(${Math.min(Math.max(m.media, 0), 10) / 10})`;
    barra.classList.toggle('medidor__preenchido--aprovado', m.mediaAtingida);
    const trilho = $('detalhe-trilho-media');
    trilho.setAttribute('aria-valuenow', String(m.media));
    trilho.setAttribute('aria-valuetext', `${n(m.media)} de 10${m.mediaAtingida ? ', média mínima atingida' : ''}`);

    const marca = $('marca-media');
    marca.style.left = `${(m.mediaMinima / 10) * 100}%`;
    $('marca-media-numero').textContent = n(m.mediaMinima);

    let nota;
    if (m.mediaAtingida) {
      nota = `Média mínima de ${n(m.mediaMinima)} atingida.`;
    } else if (!u1.avaliacoes && !u2.avaliacoes) {
      nota = `Nenhuma nota lançada. A média mínima para aprovação é ${n(m.mediaMinima)}.`;
    } else if (u1.avaliacoes && !u2.avaliacoes) {
      nota = m.notaNecessaria <= 10
        ? `Precisa de ${n(m.notaNecessaria)} na 2ª unidade para chegar à média ${n(m.mediaMinima)}.`
        : `Mesmo com 10 na 2ª unidade, a média não chega a ${n(m.mediaMinima)}.`;
    } else {
      nota = `Faltam ${n(m.notaNecessaria)} pontos, somando as unidades, para a média ${n(m.mediaMinima)}.`;
    }
    $('detalhe-media-nota').textContent = nota;
  }

  function desenharFaltas(m) {
    const vezes = m.faltasRegistradas;
    const restam = m.limiteDeFaltas - m.faltas;
    $('detalhe-faltas').textContent = `${m.faltas} de ${m.limiteDeFaltas} aulas`;

    const barra = $('detalhe-barra-faltas');
    barra.max = m.limiteDeFaltas;
    barra.value = Math.min(m.faltas, m.limiteDeFaltas);
    barra.classList.toggle('barra--perigo', perto(m) || m.reprovadoPorFalta);

    const registradas = vezes === 0 ? 'Nenhuma falta registrada.' : `${plural(vezes, 'falta registrada', 'faltas registradas')}.`;
    let nota;
    if (m.reprovadoPorFalta) nota = `${registradas} ${m.faltas - m.limiteDeFaltas} aulas acima do limite.`;
    else if (restam === 0) nota = `${registradas} No limite: a próxima falta reprova.`;
    else nota = `${registradas} Ainda pode faltar ${plural(restam, 'aula', 'aulas')}.`;
    $('detalhe-faltas-nota').textContent = nota;

    const alerta = $('alerta-reprovado');
    alerta.hidden = !m.reprovadoPorFalta;
    alerta.textContent = m.reprovadoPorFalta
      ? `Reprovado por falta: ${m.faltas} aulas, acima do limite de ${m.limiteDeFaltas}. `
        + 'As novas faltas continuam sendo registradas para o histórico ficar completo.'
      : '';

    const desfazer = $('botao-desfazer-falta');
    desfazer.disabled = m.faltas === 0;
    desfazer.classList.remove('botao--ocupado');
    const registrar = $('botao-falta');
    registrar.disabled = false;
    registrar.classList.remove('botao--ocupado');
  }

  /* Notas agrupadas por unidade: "1ª unidade · 7 de 10" e as avaliações de cada uma. */
  function desenharUnidades(m) {
    const alvo = $('detalhe-unidades');
    alvo.replaceChildren();
    m.unidades.forEach((u) => {
      const bloco = Tela.el('section', 'unidade');
      const topo = Tela.el('div', 'unidade__topo');
      topo.append(Tela.el('h3', 'unidade__titulo', `${u.numero}ª unidade`), Tela.el('span', 'unidade__nota', `${n(u.nota)} de ${n(u.notaMaxima)}`));
      const lista = Tela.el('ul', 'notas');
      const daUnidade = m.avaliacoes.filter((a) => a.unidade === u.numero);
      if (!daUnidade.length) {
        lista.append(Tela.el('li', 'notas__vazio', 'Nenhuma nota lançada nesta unidade.'));
      }
      daUnidade.forEach((a) => lista.append(linhaDeNota(m, a)));
      bloco.append(topo, lista);
      alvo.append(bloco);
    });

    // Sugere a primeira unidade que ainda não chegou a 10 pontos.
    const livre = m.unidades.find((u) => u.nota < u.notaMaxima);
    if (livre) $('form-lancar').unidade.value = String(livre.numero);
  }

  /* Cada nota tem três modos: leitura, edição e confirmação de exclusão. */
  function linhaDeNota(m, a) {
    const rotulo = `${NOMES_DOS_TIPOS[a.tipo] || a.tipo} da ${a.unidade}ª unidade`;
    const li = Tela.el('li', 'nota');
    li.dataset.avaliacao = a.id;
    mostrarLeitura();
    return li;

    function mostrarLeitura() {
      li.className = 'nota';
      const alterar = botao('Alterar', 'botao--fantasma botao--pequeno', `Alterar ${rotulo}`);
      const excluir = botao('Excluir', 'botao--alerta-suave botao--pequeno', `Excluir ${rotulo}`);
      alterar.dataset.acao = 'alterar';
      alterar.addEventListener('click', mostrarEdicao);
      excluir.addEventListener('click', mostrarConfirmacao);
      const acoes = Tela.el('div', 'nota__acoes');
      acoes.append(alterar, excluir);
      li.replaceChildren(Tela.el('span', 'nota__rotulo', NOMES_DOS_TIPOS[a.tipo] || a.tipo),
        Tela.el('strong', 'nota__valor', n(a.nota)), acoes);
    }

    function mostrarEdicao() {
      li.className = 'nota nota--editando';
      const form = Tela.el('form', 'nota__form');
      form.noValidate = true;

      const unidade = seletor('unidade', OPCOES_UNIDADE, a.unidade, `Unidade de ${rotulo}`);
      const tipo = seletor('tipo', OPCOES_TIPO, a.tipo, `Tipo de ${rotulo}`);
      const campoNota = document.createElement('input');
      Object.assign(campoNota, { name: 'nota', type: 'number', min: '0', max: '10', step: '0.1', value: String(a.nota) });
      campoNota.inputMode = 'decimal';
      campoNota.setAttribute('aria-label', `Nova nota de ${rotulo}, de 0 a 10`);

      const salvar = Tela.el('button', 'botao botao--primario botao--pequeno', 'Salvar');
      salvar.type = 'submit';
      const cancelar = botao('Cancelar', 'botao--fantasma botao--pequeno');
      cancelar.addEventListener('click', () => {
        mostrarLeitura();
        li.querySelector('[data-acao="alterar"]').focus();
      });

      form.addEventListener('keydown', (evento) => { if (evento.key === 'Escape') cancelar.click(); });
      form.addEventListener('submit', async (evento) => {
        evento.preventDefault();
        const nota = lerNota(campoNota);
        if (nota === null) return;
        Tela.ocupado(salvar, true);
        try {
          guardarMatricula(await Api.alterarNota(m.id, a.id, nota, tipo.value, Number(unidade.value)));
          desenharDetalhe(m.id);
          Tela.avisar(`Nota alterada para ${n(nota)}.`);
          const linha = $('detalhe-unidades').querySelector(`[data-avaliacao="${a.id}"] [data-acao="alterar"]`);
          if (linha) linha.focus();
        } catch (falha) {
          tratarErro(falha);
          Tela.ocupado(salvar, false);
        }
      });

      const acoes = Tela.el('div', 'nota__acoes');
      acoes.append(salvar, cancelar);
      form.append(unidade, tipo, campoNota, acoes);
      li.replaceChildren(form);
      campoNota.focus();
      campoNota.select();
    }

    function mostrarConfirmacao() {
      li.className = 'nota nota--confirmando';
      const excluir = botao('Excluir', 'botao--perigo botao--pequeno');
      const cancelar = botao('Cancelar', 'botao--fantasma botao--pequeno');
      cancelar.addEventListener('click', () => {
        mostrarLeitura();
        li.querySelector('[data-acao="alterar"]').focus();
      });
      excluir.addEventListener('click', async () => {
        Tela.ocupado(excluir, true);
        try {
          guardarMatricula(await Api.excluirNota(m.id, a.id));
          desenharDetalhe(m.id);
          Tela.avisar(`${rotulo.charAt(0).toUpperCase()}${rotulo.slice(1)} excluída.`, 'aviso');
          $('titulo-avaliacoes').focus();
        } catch (falha) {
          tratarErro(falha);
          Tela.ocupado(excluir, false);
        }
      });
      const acoes = Tela.el('div', 'nota__acoes');
      acoes.append(excluir, cancelar);
      li.replaceChildren(Tela.el('span', 'nota__pergunta', `Excluir a ${rotulo.toLocaleLowerCase('pt-BR')} (nota ${n(a.nota)})?`), acoes);
      cancelar.focus();
    }
  }

  /* ---------- Lançar nota ---------- */

  const formLancar = $('form-lancar');
  formLancar.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const nota = lerNota(formLancar.nota);
    if (nota === null) return;
    const salvar = formLancar.querySelector('button[type="submit"]');
    const unidade = Number(formLancar.unidade.value);
    Tela.ocupado(salvar, true);
    try {
      guardarMatricula(await Api.lancarNota(estado.detalheAtual, nota, formLancar.tipo.value, unidade));
      desenharDetalhe(estado.detalheAtual);
      formLancar.nota.value = '';
      Tela.avisar(`Nota ${n(nota)} lançada na ${unidade}ª unidade.`);
    } catch (falha) {
      tratarErro(falha);
    } finally {
      Tela.ocupado(salvar, false);
    }
  });

  /* ---------- Faltas ---------- */

  function esconderConfirmacaoDeFalta() {
    $('confirmar-falta').hidden = true;
    $('faltas-acoes').hidden = false;
  }

  async function registrarFalta() {
    const registrar = $('botao-falta');
    Tela.ocupado(registrar, true);
    esconderConfirmacaoDeFalta();
    try {
      const atualizada = await Api.registrarFalta(estado.detalheAtual);
      guardarMatricula(atualizada);
      desenharDetalhe(estado.detalheAtual);
      Tela.avisar(atualizada.reprovadoPorFalta ? 'Falta registrada. Reprovado por falta nesta disciplina.' : 'Falta registrada.', 'aviso');
      $('botao-falta').focus();
    } catch (falha) {
      tratarErro(falha);
      Tela.ocupado(registrar, false);
    }
  }

  $('botao-falta').addEventListener('click', () => {
    const m = matriculaAtual();
    const passaDoLimite = m.faltas + AULAS_POR_FALTA > m.limiteDeFaltas;
    // Só a falta que FAZ passar do limite pede confirmação; depois disso, registra direto.
    if (passaDoLimite && !m.reprovadoPorFalta) {
      $('confirmar-falta-texto').textContent = `Esta falta passa do limite de ${m.limiteDeFaltas} aulas e o aluno fica `
        + 'reprovado por falta nesta disciplina. Registrar mesmo assim?';
      $('faltas-acoes').hidden = true;
      $('confirmar-falta').hidden = false;
      $('confirmar-falta-nao').focus();
      return;
    }
    registrarFalta();
  });

  $('confirmar-falta-sim').addEventListener('click', registrarFalta);
  $('confirmar-falta-nao').addEventListener('click', () => {
    esconderConfirmacaoDeFalta();
    $('botao-falta').focus();
  });

  $('botao-desfazer-falta').addEventListener('click', async () => {
    const desfazer = $('botao-desfazer-falta');
    Tela.ocupado(desfazer, true);
    try {
      guardarMatricula(await Api.desfazerFalta(estado.detalheAtual));
      desenharDetalhe(estado.detalheAtual);
      Tela.avisar('Última falta desfeita.');
      $('botao-falta').focus();
    } catch (falha) {
      tratarErro(falha);
      Tela.ocupado(desfazer, false);
    }
  });

  /* ---------- Sessão ---------- */

  $('botao-sair').addEventListener('click', async () => {
    try {
      await Api.sair();
    } finally {
      window.location.replace('/');
    }
  });

  window.addEventListener('hashchange', renderizar);

  async function iniciar() {
    try {
      const [aluno, disciplinas, matriculas] = await Promise.all([Api.eu(), Api.disciplinas(), Api.matriculas()]);
      $('aluno-nome').textContent = aluno.nome;
      $('aluno-ra').textContent = `RA ${aluno.ra}`;
      estado.disciplinas = disciplinas;
      estado.matriculas = matriculas;
      renderizar();
      document.querySelector('.painel').setAttribute('aria-busy', 'false');
    } catch (falha) {
      tratarErro(falha);
    }
  }

  iniciar();
})();
