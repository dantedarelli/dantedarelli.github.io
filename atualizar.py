"""
Atualiza taxa_selic.csv e inadimplencia.csv com os dados mais recentes
publicados pelo Banco Central do Brasil (API SGS - Sistema Gerenciador
de Series Temporais: https://api.bcb.gov.br).

Series utilizadas:
  - 11    -> Taxa de juros - Selic (diaria, % a.d.)      -> taxa_selic.csv
  - 21082 -> Taxa de inadimplencia da carteira de credito
             - Pessoas fisicas (mensal, %)                -> inadimplencia.csv

Uso:
    python atualizar.py

O script le a ultima data ja presente em cada CSV, busca na API do BCB
apenas o que houver de novo a partir dali e acrescenta as linhas no
final do arquivo (sem reescrever o historico existente). Se o arquivo
nao existir ou estiver vazio, baixa a serie completa.

Nao depende de bibliotecas externas (usa somente a biblioteca padrao).
"""

import csv
import json
import urllib.error
import urllib.request
from datetime import datetime, timedelta
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent

SERIES = {
    "taxa_selic.csv": {
        "codigo": 11,
        "nome": "Taxa Selic diaria",
    },
    "inadimplencia.csv": {
        "codigo": 21082,
        "nome": "Taxa de inadimplencia da carteira de credito - Pessoas fisicas",
    },
}

FORMATO_DATA = "%d/%m/%Y"


def ler_ultima_data(caminho: Path):
    if not caminho.exists():
        return None
    ultima = None
    with open(caminho, newline="", encoding="utf-8") as f:
        leitor = csv.reader(f, delimiter=";")
        next(leitor, None)  # pula cabecalho
        for linha in leitor:
            if not linha or not linha[0].strip():
                continue
            data = datetime.strptime(linha[0].strip(), FORMATO_DATA)
            if ultima is None or data > ultima:
                ultima = data
    return ultima


def buscar_serie_bcb(codigo: int, data_inicial: str = None, data_final: str = None):
    url = f"https://api.bcb.gov.br/dados/serie/bcdata.sgs.{codigo}/dados?formato=json"
    if data_inicial and data_final:
        url += f"&dataInicial={data_inicial}&dataFinal={data_final}"

    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        if e.code == 404:
            # A API do BCB responde 404 quando nao ha nenhum dado no intervalo pedido
            return []
        raise


def atualizar_csv(nome_arquivo: str, config: dict):
    caminho = BASE_DIR / nome_arquivo
    ultima_data = ler_ultima_data(caminho)
    hoje = datetime.now()
    arquivo_existe = caminho.exists()

    if ultima_data is None:
        print(f"[{nome_arquivo}] arquivo vazio ou inexistente, baixando serie completa...")
        dados = buscar_serie_bcb(config["codigo"])
    else:
        data_inicial_dt = ultima_data + timedelta(days=1)
        if data_inicial_dt.date() > hoje.date():
            print(f"[{nome_arquivo}] ja esta atualizado (ultima data: {ultima_data:%d/%m/%Y}).")
            return
        data_inicial = data_inicial_dt.strftime(FORMATO_DATA)
        data_final = hoje.strftime(FORMATO_DATA)
        print(f"[{nome_arquivo}] buscando dados de {data_inicial} ate {data_final}...")
        dados = buscar_serie_bcb(config["codigo"], data_inicial, data_final)

    if not dados:
        print(f"[{nome_arquivo}] nenhum dado novo disponivel.")
        return

    novas_linhas = []
    for item in dados:
        data = datetime.strptime(item["data"], FORMATO_DATA)
        if ultima_data and data <= ultima_data:
            continue
        novas_linhas.append((item["data"], item["valor"]))

    if not novas_linhas:
        print(f"[{nome_arquivo}] nenhum dado novo disponivel.")
        return

    with open(caminho, "a", newline="", encoding="utf-8") as f:
        escritor = csv.writer(f, delimiter=";")
        if not arquivo_existe:
            escritor.writerow(["data", "valor"])
        for linha in novas_linhas:
            escritor.writerow(linha)

    print(f"[{nome_arquivo}] {len(novas_linhas)} nova(s) linha(s) adicionada(s) "
          f"({config['nome']}). Ultima data agora: {novas_linhas[-1][0]}")


def main():
    print(f"Atualizacao iniciada em {datetime.now():%d/%m/%Y %H:%M:%S}")
    for nome_arquivo, config in SERIES.items():
        try:
            atualizar_csv(nome_arquivo, config)
        except urllib.error.URLError as e:
            print(f"[{nome_arquivo}] erro de conexao com a API do BCB: {e}")
        except Exception as e:
            print(f"[{nome_arquivo}] erro inesperado: {e}")
    print("Atualizacao concluida.")


if __name__ == "__main__":
    main()
