package org.perpectiveteam.plugins.aisummarize.config;

public class DefaultPromptTemplate {
    public static String defaultTemplate() {
        return "###Intro###" +
                "Please review the following code below:\n" +
                "Consider:\n" +
                "1. Code quality and adherence to best practices\n" +
                "2. Potential bugs or edge cases\n" +
                "3. Performance optimizations\n" +
                "4. Readability and maintainability\n" +
                "5. Any security concerns\n" +
                "Suggest improvements and explain your reasoning for each suggestion.\n." +
                "Provide a summary in markdown markup.\n" +
                "###Intro end###\n" +
                "###After###\n" +
                "Provide a consolidated summary of the changes above.\n" +
                "###After end###";
    }
}
