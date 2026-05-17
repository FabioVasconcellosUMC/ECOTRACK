const REGEX_EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/

const apenasDigitos = (valor) => String(valor ?? '').replace(/\D/g, '')

export const validarObrigatorio = (valor, rotulo = 'Campo') => {
  if (!valor || String(valor).trim().length === 0) {
    return `${rotulo} é obrigatório.`
  }
  return ''
}

export const validarTamanhoMinimo = (valor, minimo, rotulo = 'Campo') => {
  if (!valor || String(valor).trim().length < minimo) {
    return `${rotulo} deve ter pelo menos ${minimo} caracteres.`
  }
  return ''
}

export const validarEmail = (valor) => {
  if (!valor || String(valor).trim().length === 0) {
    return 'E-mail é obrigatório.'
  }
  if (!REGEX_EMAIL.test(String(valor).trim())) {
    return 'Digite um e-mail válido.'
  }
  return ''
}

export const validarTelefone = (valor) => {
  const digitos = apenasDigitos(valor)
  if (digitos.length === 0) return 'Telefone é obrigatório.'
  if (digitos.length < 10)  return 'Telefone incompleto.'
  return ''
}

const calcularDigitoCnpj = (digitos, pesos) => {
  const soma = digitos
    .split('')
    .reduce((acc, dig, indice) => acc + Number(dig) * pesos[indice], 0)
  const resto = soma % 11
  return resto < 2 ? 0 : 11 - resto
}

export const validarCnpj = (valor) => {
  const digitos = apenasDigitos(valor)

  if (digitos.length === 0) return 'CNPJ é obrigatório.'
  if (digitos.length !== 14) return 'CNPJ deve ter 14 dígitos.'

  if (/^(\d)\1{13}$/.test(digitos)) {
    return 'CNPJ inválido.'
  }

  const pesos1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
  const pesos2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]

  const digitoVerificador1 = calcularDigitoCnpj(digitos.slice(0, 12), pesos1)
  if (digitoVerificador1 !== Number(digitos[12])) return 'CNPJ inválido.'

  const digitoVerificador2 = calcularDigitoCnpj(digitos.slice(0, 13), pesos2)
  if (digitoVerificador2 !== Number(digitos[13])) return 'CNPJ inválido.'

  return ''
}

export const useValidacao = () => ({
  validarObrigatorio,
  validarTamanhoMinimo,
  validarEmail,
  validarTelefone,
  validarCnpj,
})
