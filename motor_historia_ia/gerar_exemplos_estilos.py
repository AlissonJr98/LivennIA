import sys
import os
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import motor

os.makedirs("motor_historia_ia/dados/estilos_exemplos", exist_ok=True)

for estilo_id, info in motor.ESTILOS_RETRATO.items():
    caminho = f"motor_historia_ia/dados/estilos_exemplos/{estilo_id}.jpg"
    if os.path.exists(caminho) and os.path.getsize(caminho) > 5000:
        print(f"Already exists: {estilo_id}")
        continue

    print(f"Generating for: {estilo_id}")
    prompt_style = info["prompt_en"]
    prompt = f"Close-up character portrait, highly aesthetic face, {prompt_style}"
    sucesso = False
    for tentativa in range(3):
        try:
            # limpa qualquer trava de cota local
            with motor._trava_imagem:
                motor._sem_cota_ate.pop("pollinations", None)
            img = motor._retrato_pollinations(prompt, traduzir=False)
            if img and len(img) > 1000:
                with open(caminho, "wb") as f:
                    f.write(img)
                print(f"Saved: {estilo_id} ({len(img)} bytes)")
                sucesso = True
                break
        except Exception as e:
            print(f"Tentativa {tentativa+1} falhou para {estilo_id}: {e}")
            time.sleep(15)
    time.sleep(20)

print("Style samples generation completed!")
