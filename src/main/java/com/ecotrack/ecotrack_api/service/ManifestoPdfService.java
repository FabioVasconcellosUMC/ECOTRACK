package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.Transporte;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ManifestoPdfService {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String VAZIO = "-";

    private final TransporteService transporteService;

    public byte[] gerarManifesto(Long transporteId) {
        Transporte transporte = transporteService.buscarPorId(transporteId);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 42, 42, 42, 42);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            adicionarCabecalho(document, transporte);
            adicionarDadosTransporte(document, transporte);
            adicionarDadosLote(document, transporte.getLote());
            adicionarDadosEmpresas(document, transporte);
            adicionarRodape(document);

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar manifesto PDF", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro inesperado ao gerar manifesto PDF", e);
        }
    }

    public String nomeArquivo(Long transporteId) {
        return "manifesto-transporte-" + transporteId + ".pdf";
    }

    private void adicionarCabecalho(Document document, Transporte transporte) {
        Paragraph titulo = new Paragraph("EcoTrack", fonteTitulo());
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("Manifesto de Transporte de Residuos", fonteSubtitulo());
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(8);
        document.add(subtitulo);

        Paragraph identificacao = new Paragraph("Manifesto #" + valor(transporte.getId()), fonteTexto());
        identificacao.setAlignment(Element.ALIGN_CENTER);
        identificacao.setSpacingAfter(18);
        document.add(identificacao);
    }

    private void adicionarDadosTransporte(Document document, Transporte transporte) {
        PdfPTable tabela = tabelaSecao("Dados do Transporte");
        adicionarLinha(tabela, "Status", valor(transporte.getStatus()));
        adicionarLinha(tabela, "Responsavel", valor(transporte.getResponsavel()));
        adicionarLinha(tabela, "Data de criacao", formatarData(transporte.getCriadoEm()));
        adicionarLinha(tabela, "Data de coleta", formatarData(transporte.getDataColeta()));
        adicionarLinha(tabela, "Data de entrega", formatarData(transporte.getDataEntrega()));
        adicionarLinha(tabela, "Observacao", valor(transporte.getObservacao()));
        document.add(tabela);
    }

    private void adicionarDadosLote(Document document, Lote lote) {
        PdfPTable tabela = tabelaSecao("Dados do Lote");
        adicionarLinha(tabela, "Lote", "#" + valor(lote != null ? lote.getId() : null));
        adicionarLinha(tabela, "Descricao", valor(lote != null ? lote.getDescricao() : null));
        adicionarLinha(tabela, "Tipo de residuo", valor(lote != null ? lote.getTipoResiduo() : null));
        adicionarLinha(tabela, "Quantidade", quantidade(lote));
        adicionarLinha(tabela, "Status do lote", valor(lote != null ? lote.getStatus() : null));
        document.add(tabela);
    }

    private void adicionarDadosEmpresas(Document document, Transporte transporte) {
        PdfPTable tabela = tabelaSecao("Empresas Envolvidas");
        adicionarEmpresa(tabela, "Geradora", transporte.getLote() != null ? transporte.getLote().getEmpresaGeradora() : null);
        adicionarEmpresa(tabela, "Transportadora", transporte.getTransportadora());
        adicionarEmpresa(tabela, "Receptora", transporte.getReceptora());
        document.add(tabela);
    }

    private void adicionarRodape(Document document) {
        Paragraph declaracao = new Paragraph(
                "Documento gerado automaticamente pelo EcoTrack para apoio ao controle, auditoria e rastreabilidade operacional.",
                fontePequena()
        );
        declaracao.setSpacingBefore(18);
        declaracao.setAlignment(Element.ALIGN_CENTER);
        document.add(declaracao);

        Paragraph geradoEm = new Paragraph("Gerado em " + formatarData(LocalDateTime.now()), fontePequena());
        geradoEm.setAlignment(Element.ALIGN_CENTER);
        document.add(geradoEm);
    }

    private PdfPTable tabelaSecao(String titulo) {
        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(10);
        tabela.setSpacingAfter(8);
        tabela.setWidths(new float[]{28, 72});

        PdfPCell cabecalho = new PdfPCell(new Phrase(titulo, fonteSecao()));
        cabecalho.setColspan(2);
        cabecalho.setPadding(8);
        cabecalho.setHorizontalAlignment(Element.ALIGN_LEFT);
        tabela.addCell(cabecalho);

        return tabela;
    }

    private void adicionarEmpresa(PdfPTable tabela, String papel, Empresa empresa) {
        String dados = valor(empresa != null ? empresa.getRazaoSocial() : null)
                + "\nCNPJ: " + valor(empresa != null ? empresa.getCnpj() : null)
                + "\nE-mail: " + valor(empresa != null ? empresa.getEmail() : null)
                + "\nTelefone: " + valor(empresa != null ? empresa.getTelefone() : null)
                + "\nEndereco: " + valor(empresa != null ? empresa.getEndereco() : null);
        adicionarLinha(tabela, papel, dados);
    }

    private void adicionarLinha(PdfPTable tabela, String rotulo, String valor) {
        PdfPCell celulaRotulo = new PdfPCell(new Phrase(rotulo, fonteRotulo()));
        celulaRotulo.setPadding(7);
        tabela.addCell(celulaRotulo);

        PdfPCell celulaValor = new PdfPCell(new Phrase(valor(valor), fonteTexto()));
        celulaValor.setPadding(7);
        tabela.addCell(celulaValor);
    }

    private String quantidade(Lote lote) {
        if (lote == null) {
            return VAZIO;
        }

        BigDecimal quantidade = lote.getQuantidade();
        String unidade = valor(lote.getUnidade());
        return quantidade != null ? quantidade.stripTrailingZeros().toPlainString() + " " + unidade : VAZIO;
    }

    private String formatarData(LocalDateTime data) {
        return data != null ? data.format(DATA_HORA) : VAZIO;
    }

    private String valor(Object valor) {
        return valor != null && !valor.toString().isBlank() ? valor.toString() : VAZIO;
    }

    private Font fonteTitulo() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
    }

    private Font fonteSubtitulo() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    }

    private Font fonteSecao() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    }

    private Font fonteRotulo() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    }

    private Font fonteTexto() {
        return FontFactory.getFont(FontFactory.HELVETICA, 10);
    }

    private Font fontePequena() {
        return FontFactory.getFont(FontFactory.HELVETICA, 8);
    }
}
