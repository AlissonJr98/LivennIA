import os
from dotenv import load_dotenv
from google import genai

PASTA = os.path.dirname(os.path.abspath(__file__))
load_dotenv(os.path.join(PASTA, ".env"))

cliente = genai.Client(api_key=os.environ.get("GEMINI_API_KEY"))

for modelo in cliente.models.list():
    acoes = modelo.supported_actions or []
    if "generateContent" in acoes:
        print(modelo.name)