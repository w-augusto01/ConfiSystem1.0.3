package br.com.confisystem.service;

import br.com.confisystem.dto.NfeImportada;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NfeXmlParser {

    private static final Set<String> UNIDADES_KG =
            Set.of("KG", "KGS", "QUILO", "QUILOS", "KILO", "KILOS", "QUILOGRAMA", "QUILOGRAMAS");
    private static final Set<String> UNIDADES_TONELADA =
            Set.of("T", "TN", "TON", "TONS", "TONELADA", "TONELADAS", "TO");
    private static final Set<String> UNIDADES_LITRO =
            Set.of("L", "LT", "LTS", "LITRO", "LITROS");
    private static final Set<String> UNIDADES_GRAMA =
            Set.of("G", "GR", "GRAMA", "GRAMAS");

    private static final Pattern KG_NO_NOME =
            Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:KGS?|QUILOS?)\\b", Pattern.CASE_INSENSITIVE);

    private NfeXmlParser() {
    }

    public static NfeImportada ler(InputStream entrada) {

        Document doc;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            // Seguranca: XML vem de terceiros -> bloqueia DTD/entidades externas (XXE)
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            doc = dbf.newDocumentBuilder().parse(entrada);
        } catch (Exception e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo. Verifique se é um XML de NF-e válido.", e);
        }

        Element raiz = doc.getDocumentElement();
        Element infNFe = primeiro(raiz, "infNFe");

        if (infNFe == null) {
            throw new IllegalArgumentException(
                    "Este XML não é uma NF-e completa (não encontrei os dados da nota). " +
                            "Use o XML integral da nota, não o resumo ou um evento.");
        }

        NfeImportada nfe = new NfeImportada();

        nfe.setChaveAcesso(lerChave(raiz, infNFe));
        nfe.setNumero(texto(primeiro(infNFe, "ide"), "nNF"));
        nfe.setDataEmissao(lerData(primeiro(infNFe, "ide")));

        Element emit = primeiro(infNFe, "emit");
        if (emit != null) {
            String doc1 = texto(emit, "CNPJ");
            if (doc1 == null) {
                doc1 = texto(emit, "CPF");
            }
            nfe.setDocumentoEmitente(doc1 == null ? null : doc1.replaceAll("\\D", ""));
            nfe.setNomeEmitente(texto(emit, "xNome"));
        }

        Element totais = primeiro(infNFe, "ICMSTot");
        if (totais != null) {
            nfe.setValorProdutos(decimal(totais, "vProd"));
            nfe.setFrete(decimal(totais, "vFrete"));
            nfe.setSeguro(decimal(totais, "vSeg"));
            nfe.setOutrasDespesas(decimal(totais, "vOutro"));
            nfe.setDesconto(decimal(totais, "vDesc"));
            nfe.setValorNota(decimal(totais, "vNF"));
        }

        NodeList dets = infNFe.getElementsByTagNameNS("*", "det");

        for (int i = 0; i < dets.getLength(); i++) {

            Element prod = primeiro((Element) dets.item(i), "prod");

            if (prod == null) {
                continue;
            }

            NfeImportada.Item item = new NfeImportada.Item();
            item.setCodigoProduto(texto(prod, "cProd"));
            item.setDescricao(texto(prod, "xProd"));
            item.setUnidadeNota(normalizarUnidade(texto(prod, "uCom")));
            item.setQuantidadeNota(decimal(prod, "qCom"));
            item.setValorProduto(decimal(prod, "vProd"));
            item.setUnidadeTributavel(normalizarUnidade(texto(prod, "uTrib")));
            item.setQuantidadeTributavel(decimal(prod, "qTrib"));

            nfe.getItens().add(item);
        }

        if (nfe.getItens().isEmpty()) {
            throw new IllegalArgumentException("O XML não contém nenhum item de produto.");
        }

        return nfe;
    }

    public static void sugerirFator(NfeImportada.Item item) {

        String un = item.getUnidadeNota();

        if (un != null) {
            if (UNIDADES_KG.contains(un) || UNIDADES_LITRO.contains(un)) {
                definir(item, BigDecimal.ONE, "unidade da nota (" + un + ")");
                return;
            }
            if (UNIDADES_TONELADA.contains(un)) {
                definir(item, BigDecimal.valueOf(1000), "unidade da nota (" + un + ")");
                return;
            }
            if (UNIDADES_GRAMA.contains(un)) {
                definir(item, new BigDecimal("0.001"), "unidade da nota (" + un + ")");
                return;
            }
        }

        if (item.getUnidadeTributavel() != null
                && UNIDADES_KG.contains(item.getUnidadeTributavel())
                && item.getQuantidadeTributavel() != null
                && item.getQuantidadeTributavel().signum() > 0
                && item.getQuantidadeNota() != null
                && item.getQuantidadeNota().signum() > 0) {

            BigDecimal fator = item.getQuantidadeTributavel()
                    .divide(item.getQuantidadeNota(), 6, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            definir(item, fator.scale() < 0 ? fator.setScale(0) : fator, "unidade tributável da nota (KG)");
            return;
        }

        if (item.getDescricao() != null) {
            Matcher m = KG_NO_NOME.matcher(item.getDescricao());
            if (m.find()) {
                BigDecimal fator = new BigDecimal(m.group(1).replace(',', '.'));
                if (fator.signum() > 0) {
                    definir(item, fator, "nome do produto (\"" + m.group().trim() + "\")");
                    return;
                }
            }
        }

        item.setFatorParaKg(null);
        item.setOrigemFator(null);
    }

    private static void definir(NfeImportada.Item item, BigDecimal fator, String origem) {
        item.setFatorParaKg(fator);
        item.setOrigemFator(origem);
    }

    public static String normalizarUnidade(String unidade) {
        if (unidade == null) {
            return "";
        }
        return unidade.trim().toUpperCase().replace(".", "");
    }

    private static String lerChave(Element raiz, Element infNFe) {

        String id = infNFe.getAttribute("Id");

        if (id != null && id.startsWith("NFe")) {
            id = id.substring(3);
        }

        if (id == null || id.length() != 44) {
            String ch = texto(raiz, "chNFe");
            if (ch != null && ch.length() == 44) {
                return ch;
            }
        }

        return id == null || id.isBlank() ? null : id;
    }

    private static LocalDate lerData(Element ide) {

        String data = texto(ide, "dhEmi");

        if (data == null) {
            data = texto(ide, "dEmi"); // layout antigo
        }

        if (data == null || data.length() < 10) {
            return null;
        }

        return LocalDate.parse(data.substring(0, 10));
    }

    private static Element primeiro(Element contexto, String tag) {

        if (contexto == null) {
            return null;
        }

        NodeList lista = contexto.getElementsByTagNameNS("*", tag);

        return lista.getLength() == 0 ? null : (Element) lista.item(0);
    }

    private static String texto(Element contexto, String tag) {

        Element e = primeiro(contexto, tag);

        if (e == null) {
            return null;
        }

        String valor = e.getTextContent();

        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static BigDecimal decimal(Element contexto, String tag) {

        String valor = texto(contexto, tag);

        if (valor == null) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido no XML (" + tag + "): " + valor);
        }
    }
}
