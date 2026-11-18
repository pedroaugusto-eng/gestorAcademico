(() => {
  const vistas = {
    entrar: document.getElementById('vista-entrar'),
    cadastro: document.getElementById('vista-cadastro'),
  };
  const formEntrar = document.getElementById('form-entrar');
  const formCadastro = document.getElementById('form-cadastro');

  /* Troca entre "Entrar" e "Criar acesso" e leva o foco para o título da nova tela. */
  function mostrar(nome) {
    Object.entries(vistas).forEach(([chave, vista]) => {
      vista.hidden = chave !== nome;
    });
    const vista = vistas[nome];
    vista.classList.remove('acesso--surgindo');
    void vista.offsetWidth; // reinicia a animação curta de entrada
    vista.classList.add('acesso--surgindo');
    vista.querySelector('.acesso__titulo').focus();
    document.title = `Gestor Acadêmico · ${nome === 'entrar' ? 'Entrar' : 'Criar acesso'}`;
  }

  document.querySelectorAll('[data-ir-para]').forEach((botao) => {
    botao.addEventListener('click', () => mostrar(botao.dataset.irPara));
  });

  /* Botão Mostrar/Ocultar senha. */
  document.querySelectorAll('[data-alternar-senha]').forEach((botao) => {
    const campo = document.getElementById(botao.dataset.alternarSenha);
    botao.addEventListener('click', () => {
      const visivel = campo.type === 'text';
      campo.type = visivel ? 'password' : 'text';
      botao.textContent = visivel ? 'Mostrar' : 'Ocultar';
      botao.setAttribute('aria-pressed', String(!visivel));
      campo.focus();
    });
  });

  /* Máscara simples de CPF enquanto digita. */
  const campoCpf = document.getElementById('cpf');
  campoCpf.addEventListener('input', () => {
    const d = campoCpf.value.replace(/\D/g, '').slice(0, 11);
    let formatado = d;
    if (d.length > 9) formatado = `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`;
    else if (d.length > 6) formatado = `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6)}`;
    else if (d.length > 3) formatado = `${d.slice(0, 3)}.${d.slice(3)}`;
    campoCpf.value = formatado;
  });

  function ocupado(botao, sim, textoOcupado) {
    if (sim) {
      botao.dataset.textoOriginal = botao.textContent;
      botao.textContent = textoOcupado;
    } else if (botao.dataset.textoOriginal) {
      botao.textContent = botao.dataset.textoOriginal;
    }
    Tela.ocupado(botao, sim);
  }

  formEntrar.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const erro = document.getElementById('erro-entrar');
    const botao = formEntrar.querySelector('button[type="submit"]');
    const identificador = formEntrar.identificador.value.trim();
    const senha = formEntrar.senha.value;
    erro.textContent = '';

    if (!identificador || !senha) {
      erro.textContent = 'Preencha o RA ou CPF e a senha.';
      (identificador ? formEntrar.senha : formEntrar.identificador).focus();
      return;
    }

    ocupado(botao, true, 'Entrando…');
    try {
      await Api.entrar(identificador, senha);
      window.location.assign('/painel.html');
    } catch (falha) {
      erro.textContent = falha.message;
      formEntrar.senha.value = '';
      formEntrar.senha.focus();
      ocupado(botao, false);
    }
  });

  formCadastro.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const erro = document.getElementById('erro-cadastro');
    const botao = formCadastro.querySelector('button[type="submit"]');
    const aluno = {
      nome: formCadastro.nome.value.trim(),
      cpf: formCadastro.cpf.value.trim(),
      ra: formCadastro.ra.value.trim(),
      email: formCadastro.email.value.trim(),
      senha: formCadastro.senha.value,
    };
    erro.textContent = '';
    Tela.mostrarErrosDeCampo(formCadastro, {});

    ocupado(botao, true, 'Criando acesso…');
    try {
      await Api.cadastrar(aluno);
      await Api.entrar(aluno.ra, aluno.senha);
      window.location.assign('/painel.html');
    } catch (falha) {
      Tela.mostrarErrosDeCampo(formCadastro, falha.campos);
      erro.textContent = falha.message;
      const primeiroInvalido = formCadastro.querySelector('[aria-invalid="true"]');
      if (primeiroInvalido) primeiroInvalido.focus();
      ocupado(botao, false);
    }
  });

  /* Quem já está logado vai direto para o painel. */
  Api.eu().then(() => window.location.replace('/painel.html')).catch(() => {});
})();
