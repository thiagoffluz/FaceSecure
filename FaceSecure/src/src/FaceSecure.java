package src;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FaceSecure {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            System.out.println("Erro ao configurar saída UTF-8.");
        }

        Scanner scanner = new Scanner(System.in);
        int opcao;

        do {
            System.out.println("\n===== FaceSecure - Controle de Acesso =====");
            System.out.println("1 - Capturar e Cadastrar um novo rosto");
            System.out.println("2 - Verificar acesso de um novo rosto");
            System.out.println("0 - Sair");
            System.out.print("Escolha a opção: ");
            opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    capturarERegistrarRosto(scanner);
                    break;
                case 2:
                    verificarRosto(scanner);
                    break;
                case 0:
                    System.out.println("Encerrando o programa...");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        } while (opcao != 0);

        scanner.close();
        System.exit(0);
    }

    private static void aguardarConfirmacao(VideoCapture camera, String janela, String mensagem) {
        Mat frame = new Mat();
        System.out.println(mensagem);
        System.out.println("Pressione ENTER no terminal para iniciar...");

        Thread threadTeclado = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            sc.nextLine();
        });
        threadTeclado.start();

        while (threadTeclado.isAlive()) {
            camera.read(frame);
            if (!frame.empty()) {
                HighGui.imshow(janela, frame);
                HighGui.waitKey(30);
            }
        }

        HighGui.destroyWindow(janela);
    }

    private static void capturarERegistrarRosto(Scanner scanner) {
        CascadeClassifier detector = new CascadeClassifier("src/haarcascade_frontalface_default.xml");
        VideoCapture camera = new VideoCapture(0);

        if (!camera.isOpened()) {
            System.out.println("Não foi possível abrir a webcam.");
            return;
        }

        File pasta = new File("rostos_conhecidos");
        if (!pasta.exists()) {
            pasta.mkdir();
        }

        File[] arquivos = pasta.listFiles((dir, name) -> name.endsWith(".jpg"));
        int contadorPessoa = (arquivos != null ? arquivos.length : 0) + 1;

        aguardarConfirmacao(camera, "Pré-visualização - Cadastro", "Posicione-se. Serão capturadas 10 imagens (uma por segundo).");

        Mat frame = new Mat();
        int imagensCapturadas = 0;
        long ultimoTempo = System.currentTimeMillis();

        while (imagensCapturadas < 10) {
            camera.read(frame);
            if (frame.empty()) continue;

            Mat imagemCinza = new Mat();
            Imgproc.cvtColor(frame, imagemCinza, Imgproc.COLOR_BGR2GRAY);

            MatOfRect rostos = new MatOfRect();
            detector.detectMultiScale(imagemCinza, rostos);

            for (Rect rosto : rostos.toArray()) {
                if (System.currentTimeMillis() - ultimoTempo >= 1000) {
                    Mat rostoRecortado = new Mat(frame, rosto);
                    Imgproc.resize(rostoRecortado, rostoRecortado, new Size(200, 200));

                    String nomeArquivo = String.format("rostos_conhecidos/pessoa_%d_img_%d.jpg", contadorPessoa, imagensCapturadas + 1);
                    Imgcodecs.imwrite(nomeArquivo, rostoRecortado);
                    System.out.println("✅ Imagem de cadastro salva: " + nomeArquivo);

                    imagensCapturadas++;
                    ultimoTempo = System.currentTimeMillis();
                }
            }

            HighGui.imshow("Captura de Rosto", frame);
            HighGui.waitKey(30);
        }

        camera.release();
        HighGui.destroyAllWindows();
    }

    private static void verificarRosto(Scanner scanner) {
        CascadeClassifier detector = new CascadeClassifier("src/haarcascade_frontalface_default.xml");
        VideoCapture camera = new VideoCapture(0);

        if (!camera.isOpened()) {
            System.out.println("Não foi possível abrir a webcam.");
            return;
        }

        aguardarConfirmacao(camera, "Pré-visualização - Verificação", "Posicione-se para verificação de acesso.");

        Mat frame = new Mat();
        List<Mat> rostosCapturados = new ArrayList<>();
        int imagensCapturadas = 0;
        long ultimoTempo = System.currentTimeMillis();

        System.out.println("Capturando múltiplas imagens... (5 imagens, uma por segundo)");

        while (imagensCapturadas < 5) {
            camera.read(frame);
            if (frame.empty()) continue;

            Mat imagemCinza = new Mat();
            Imgproc.cvtColor(frame, imagemCinza, Imgproc.COLOR_BGR2GRAY);

            MatOfRect rostos = new MatOfRect();
            detector.detectMultiScale(imagemCinza, rostos);

            for (Rect rosto : rostos.toArray()) {
                // Quadrado roxo durante verificação
                Imgproc.rectangle(frame, rosto.tl(), rosto.br(), new Scalar(255, 0, 255), 2);

                if (System.currentTimeMillis() - ultimoTempo >= 1000) {
                    Mat rostoCapturado = new Mat(frame, rosto);
                    Imgproc.resize(rostoCapturado, rostoCapturado, new Size(200, 200));
                    rostosCapturados.add(rostoCapturado);
                    imagensCapturadas++;
                    System.out.println("Imagem " + imagensCapturadas + " capturada.");
                    ultimoTempo = System.currentTimeMillis();
                }
            }

            HighGui.imshow("Verificação de Acesso", frame);
            HighGui.waitKey(30);
        }

        camera.release();
        HighGui.destroyAllWindows();

        boolean reconhecido = false;

        for (int i = 0; i < rostosCapturados.size(); i++) {
            Mat rostoEntrada = rostosCapturados.get(i);
            System.out.println("\n🔎 Analisando imagem capturada número: " + (i + 1));

            if (compararComBase(rostoEntrada)) {
                reconhecido = true;
            }
        }

        if (reconhecido) {
            System.out.println("✅ Acesso Permitido: Rosto reconhecido em pelo menos uma captura!");
        } else {
            System.out.println("❌ Acesso Negado: Nenhum rosto reconhecido.");
        }
    }

    private static boolean compararComBase(Mat rostoEntrada) {
        File pasta = new File("rostos_conhecidos");
        File[] arquivos = pasta.listFiles((dir, name) -> name.endsWith(".jpg"));

        if (arquivos == null || arquivos.length == 0) {
            System.out.println("Nenhum rosto cadastrado para comparação.");
            return false;
        }

        Imgproc.cvtColor(rostoEntrada, rostoEntrada, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(rostoEntrada, rostoEntrada);
        Imgproc.GaussianBlur(rostoEntrada, rostoEntrada, new Size(3, 3), 0);

        Mat histEntrada = new Mat();
        Imgproc.calcHist(List.of(rostoEntrada), new MatOfInt(0), new Mat(), histEntrada, new MatOfInt(64), new MatOfFloat(0, 256));
        Core.normalize(histEntrada, histEntrada);

        boolean reconhecido = false;

        for (File arquivo : arquivos) {
            Mat rostoCadastrado = Imgcodecs.imread(arquivo.getAbsolutePath());
            Imgproc.cvtColor(rostoCadastrado, rostoCadastrado, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(rostoCadastrado, rostoCadastrado);
            Imgproc.GaussianBlur(rostoCadastrado, rostoCadastrado, new Size(3, 3), 0);

            Mat histCadastrado = new Mat();
            Imgproc.calcHist(List.of(rostoCadastrado), new MatOfInt(0), new Mat(), histCadastrado, new MatOfInt(64), new MatOfFloat(0, 256));
            Core.normalize(histCadastrado, histCadastrado);

            double resultado = Imgproc.compareHist(histEntrada, histCadastrado, Imgproc.CV_COMP_CORREL);
            System.out.printf("→ Comparando com %s - Similaridade: %.5f\n", arquivo.getName(), resultado);

            if (resultado > 0.50) {
                reconhecido = true;
            }
        }

        return reconhecido;
    }
}