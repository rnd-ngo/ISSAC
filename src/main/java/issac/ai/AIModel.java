package issac.ai;

import java.nio.file.Path;

public interface AIModel {
    String analyzeText(String text);
    String analyzeImage(Path imagePath);
}