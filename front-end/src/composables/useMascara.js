const apenasDigitos = (valor) => String(valor ?? '').replace(/\D/g, '')

const aplicarPadrao = (digitos, padrao) => {
  let resultado = ''
  let indiceDigito = 0

  for (let i = 0; i < padrao.length && indiceDigito < digitos.length; i++) {
    const caractere = padrao[i]
    if (caractere === '#') {
      resultado += digitos[indiceDigito]
      indiceDigito++
    } else {
      resultado += caractere
    }
  }

  return resultado
}

const formatarCnpj = (valor) => {
  const digitos = apenasDigitos(valor).slice(0, 14)
  return aplicarPadrao(digitos, '##.###.###/####-##')
}

const formatarCpf = (valor) => {
  const digitos = apenasDigitos(valor).slice(0, 11)
  return aplicarPadrao(digitos, '###.###.###-##')
}

const formatarTelefone = (valor) => {
  const digitos = apenasDigitos(valor).slice(0, 11)
  const padrao = digitos.length > 10 ? '(##) #####-####' : '(##) ####-####'
  return aplicarPadrao(digitos, padrao)
}

const formatarCep = (valor) => {
  const digitos = apenasDigitos(valor).slice(0, 8)
  return aplicarPadrao(digitos, '#####-###')
}

const formatadores = {
  cnpj:     formatarCnpj,
  cpf:      formatarCpf,
  telefone: formatarTelefone,
  cep:      formatarCep,
}

export const formatarComMascara = (valor, tipoMascara) => {
  const formatador = formatadores[tipoMascara]
  if (!formatador) return valor
  return formatador(valor)
}

export const removerMascara = (valor) => apenasDigitos(valor)

export const useMascara = () => ({
  formatarComMascara,
  removerMascara,
})
