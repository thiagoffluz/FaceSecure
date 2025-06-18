import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class Main {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // ou "opencv_java4110"
    }

    public static void main(String[] args) {
        if (testeFuncionamento()) {
            processarImagemComHaarCascade("src/entrada.jpg", "saida_etapa1.jpg");
        } else {
            System.out.println("OpenCV não está funcionando");
        }
    }

    public static boolean testeFuncionamento() {
        System.out.println("OpenCV carregado com sucesso!");
        System.out.println("Versão: " + Core.VERSION);
        return true;
    }

    public static void processarImagemComHaarCascade(String caminhoEntrada, String caminhoSaida) {
        // Carregar imagem
        Mat imagemColorida = Imgcodecs.imread(caminhoEntrada);
        if (imagemColorida.empty()) {
            System.out.println("Não foi possível carregar a imagem.");
            return;
        }

        // Converter para escala de cinza
        Mat imagemCinza = new Mat();
        Imgproc.cvtColor(imagemColorida, imagemCinza, Imgproc.COLOR_BGR2GRAY);

        // Carregar o classificador Haar Cascade
        CascadeClassifier detector = new CascadeClassifier("haarcascade_frontalface_default.xml");
        if (detector.empty()) {
            System.out.println("Não foi possível carregar o classificador Haar Cascade.");
            return;
        }

        // Detectar objetos (ex: faces)
        MatOfRect deteccoes = new MatOfRect();
        detector.detectMultiScale(imagemCinza, deteccoes);

        // Desenhar retângulos verdes ao redor das detecções
        for (Rect ret : deteccoes.toArray()) {
            Imgproc.rectangle(imagemColorida, ret.tl(), ret.br(), new Scalar(0, 255, 0), 2);
        }

        // Salvar imagem resultante
        Imgcodecs.imwrite(caminhoSaida, imagemColorida);
        System.out.println("Imagem processada e salva como " + caminhoSaida);
    }
}
