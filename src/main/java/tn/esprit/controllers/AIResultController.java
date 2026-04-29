package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class AIResultController {

    @FXML private WebView resultWebView;

    public void setResultText(String text) {
        // Convert Markdown to basic HTML
        String htmlText = text;
        // Bold
        htmlText = htmlText.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");
        // Italic
        htmlText = htmlText.replaceAll("\\*(.*?)\\*", "<em>$1</em>");
        // Headers
        htmlText = htmlText.replaceAll("### (.*)", "<h3>$1</h3>");
        htmlText = htmlText.replaceAll("## (.*)", "<h2>$1</h2>");
        // Newlines to <br>
        htmlText = htmlText.replaceAll("\n", "<br/>");
        // Bullet points
        htmlText = htmlText.replaceAll("\\* (.*?)<br/>", "<li>$1</li>");
        // Clean up remaining asterisks that might have been used for bullets
        htmlText = htmlText.replace("*", "");

        String content = "<html><head><style>" +
            "body { background-color: transparent; color: #cccccc; font-family: 'Segoe UI', Arial, sans-serif; font-size: 14px; line-height: 1.6; padding: 10px; margin: 0; }" +
            "h2, h3 { color: #E53935; margin-top: 15px; margin-bottom: 10px; }" +
            "strong { color: #ffffff; font-weight: bold; }" +
            "li { margin-bottom: 5px; }" +
            "</style></head><body>" + htmlText + "</body></html>";

        resultWebView.getEngine().loadContent(content);
        
        // Ensure webview blends with the dark background
        resultWebView.setPageFill(Color.TRANSPARENT);
    }

    @FXML
    private void closeModal() {
        Stage stage = (Stage) resultWebView.getScene().getWindow();
        stage.close();
    }
}
