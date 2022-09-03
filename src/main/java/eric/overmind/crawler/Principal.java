package eric.overmind.crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import eric.overmind.crawler.domain.dto.CrawlerDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Principal {

	int quantidadeFilmes = 10;

	public static void main(String[] args) {
		new Principal().getImdbPage("https://www.imdb.com/chart/bottom");
	}

	private String getNomeEmIngles(String URL) {
		String retorno = "";
		try {
			Document document = Jsoup.connect("https://www.imdb.com" + URL).get();

			String tudo = document.toString();

			int inicioFilmeIngles = tudo.indexOf("Original title: ");
			String filmeInglesTemp = tudo.substring(inicioFilmeIngles).trim();
			int fimFilmeIngles = filmeInglesTemp.indexOf("</div>");
			retorno = filmeInglesTemp.substring(16, fimFilmeIngles).trim();

		} catch (Exception e) {
			log.error("-> Nome inglês igual em português");
		}

		return retorno;

	}

	public void getImdbPage(String URL) {
		try {
			Document document = Jsoup.connect(URL).get();

			String tudo = document.text();

			List<CrawlerDTO> listaCrawlerDTO = new ArrayList<CrawlerDTO>();

			for (int i = 1; i <= quantidadeFilmes; i++) {
				imprimir("Carregando filme " + i + " de " + quantidadeFilmes);
				CrawlerDTO c = new CrawlerDTO();

				c.setPosicao(i);
				
				int posicaoColocacao = tudo.indexOf(" " + i + ". ");
				String filme = tudo.substring(posicaoColocacao).trim();
				
				c.setNota(getNota(filme));

				c.setNomePortugues(getNomeFilmePortugues(filme));

				c.setDirecaoEAtoresPrincipais(getDirecaoEAtoresPrincipais(getTextoHtml(document, c)));

				c.setNomeIngles(getNomeEmIngles(getUrlFilme(getTextoHtml(document, c))));
				
				c.setNomeIngles(!c.getNomeIngles().isEmpty()?c.getNomeIngles():c.getNomePortugues());
				
				listaCrawlerDTO.add(c);
			}

			Collections.reverse(listaCrawlerDTO);

			for (CrawlerDTO c : listaCrawlerDTO) {
				String retorno = "\n******************************************" + "\n      Posição de pior filme: "
						+ c.getPosicao() + "\n Nome do filme em português: " + c.getNomePortugues()
						+ "\n    Nome do filme em inglês: " + c.getNomeIngles() + "\nDireção e atores principais: "
						+ c.getDirecaoEAtoresPrincipais() + "\n                       Nota: " + c.getNota();

				imprimir(retorno);
			}

		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	private String getTextoHtml(Document document, CrawlerDTO c) {
		String textoComHtml = document.toString();
		int posicaoTextoParte = textoComHtml.indexOf(c.getNomePortugues().replace("&", "&amp;"));
		String subtextoComHtml = textoComHtml.substring(posicaoTextoParte).trim();
		return subtextoComHtml;
	}

	private String getUrlFilme(String subtextoComHtml) {
		int fimPosicaoUrlFilme = subtextoComHtml.indexOf("\" title=");
		int inicioPosicaoUrlFilme = subtextoComHtml.indexOf(" <a href=");
		String urlFilme = subtextoComHtml.substring(inicioPosicaoUrlFilme + 10, fimPosicaoUrlFilme);
		return urlFilme;
	}

	private String getDirecaoEAtoresPrincipais(String subtextoComHtml) {
		int inicioPosicaoDirecaoEAtoresPrincipais = subtextoComHtml.indexOf("\" title=");
		String direcaoEAtoresPrincipais = subtextoComHtml.substring(inicioPosicaoDirecaoEAtoresPrincipais + 9);
		int fimPosicaoDirecaoEAtoresPrincipais = direcaoEAtoresPrincipais.indexOf("\">");

		direcaoEAtoresPrincipais = direcaoEAtoresPrincipais.substring(0, fimPosicaoDirecaoEAtoresPrincipais);
		return direcaoEAtoresPrincipais;
	}

	private String getNota(String filme) {
		int posicaoFinalFilmeNota = filme.indexOf(") ");
		int posicaoFinalNotal = filme.indexOf("   1 2 3 4 5 6 7 8 9 10");
		String notaFilme = filme.substring(posicaoFinalFilmeNota + 1, posicaoFinalNotal).trim();
		return notaFilme;
	}

	private String getNomeFilmePortugues(String filme) {
		int posicaoFinalFilmeNomePortugues = filme.indexOf(") ");
		int posicaoInicialFilme = filme.indexOf(" ");
		String nomeFilme = filme.substring(posicaoInicialFilme, posicaoFinalFilmeNomePortugues + 1);
		String nomeFilmePortugues = nomeFilme.substring(0, nomeFilme.length() - 7).trim();
		return nomeFilmePortugues;
	}

	
	private void imprimir(String texto) {
		System.out.println(texto);
	}
}
