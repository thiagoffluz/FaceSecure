# FaceSecure - Controle de Acesso por Reconhecimento Facial
Uma aplicação simples de reconhecimento facial para controle de acesso, desenvolvida utilizando Java e OpenCV. O sistema permite cadastrar rostos e posteriormente verificar se um novo rosto capturado corresponde a um dos cadastrados.

-> Funcionalidades
Cadastro de Rosto:
O sistema captura 10 imagens por pessoa durante o processo de cadastro, aumentando a base de comparação.

Verificação de Acesso:
Durante a verificação, o sistema captura 5 imagens do rosto do usuário (uma por segundo). Isso aumenta as chances de identificação mesmo com pequenas variações de posição, iluminação ou expressões faciais.

Comparação por Histograma:
Utiliza comparação de histogramas (método CV_COMP_CORREL do OpenCV) para verificar a similaridade entre as imagens capturadas e as cadastradas.

Feedback Visual:
Exibe o vídeo ao vivo da câmera com destaque visual (retângulo roxo) nas regiões onde os rostos são detectados.

-> Requisitos
Java (JDK 8 ou superior)

OpenCV (versão 4.x ou superior)

Webcam conectada e funcionando

-> Como Executar
1. Instalação do OpenCV:
Baixe o OpenCV:
https://opencv.org/releases/

Extraia e configure o path da sua biblioteca nativa (.dll, .so ou .dylib dependendo do sistema operacional).

2. Compilar o projeto:
javac -cp "caminho/para/opencv.jar" src/FaceSecure.java

3. Executar:
java -cp ".;caminho/para/opencv.jar" -Djava.library.path="caminho/para/libs/nativas" src.FaceSecure


-> Estrutura de Pastas
FaceSecure/
├── src/
│   └── FaceSecure.java
├── rostos_conhecidos/
│   └── (imagens .jpg dos rostos cadastrados)
└── README.md


-> Como Funciona
Cadastro (Opção 1):
O usuário se posiciona em frente à câmera.

Pressiona ENTER para iniciar.

O sistema captura 10 imagens em sequência.

Verificação (Opção 2):
O usuário se posiciona novamente.

Pressiona ENTER.

O sistema captura 5 imagens e compara com as imagens existentes.

Se pelo menos uma imagem for semelhante (correlação > 0.50), o acesso é permitido.

-> Observações Importantes
O limiar de aceitação da similaridade está ajustado para 0.50, considerando a escala de Correlação (CV_COMP_CORREL), onde valores mais próximos de 1 indicam maior similaridade.

Este sistema é apenas um protótipo acadêmico. Não deve ser usado para controle de acesso em ambientes que exijam alta segurança.

👨‍💻 Autor
Thiago Felipe Luz
Curso: Engenharia de Software - UEPG
Disciplina: Processamento Digital de Imagens - 2025
