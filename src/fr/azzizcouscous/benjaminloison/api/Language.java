package fr.azzizcouscous.benjaminloison.api;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

import fr.azzizcouscous.benjaminloison.main.Main;

public class Language
{
    private static String example = "\"something not translated=Something translated\"";
    static Map<String, String> translations;

    public static void initialize()
    {
        if(!FileAPI.languageFile.exists())
            FileAPI.write(FileAPI.languageFile.getAbsolutePath(), example);
        loadTranslations();
    }

    private static void loadTranslations()
    {
        try
        {
            Scanner scan = new Scanner(FileAPI.languageFile);
            int lineNumber = 0;
            while(scan.hasNextLine())
            {
                String line = scan.nextLine(), parts[] = line.split("=");
                lineNumber++;
                if(parts.length != 2)
                    Main.warn("Invalid translation in line (" + lineNumber + "): " + line + " | Must be like this: " + example);
                translations.put(parts[0], parts[1]);
            }
            scan.close();
        }
        catch(FileNotFoundException e)
        {
            Main.warn("No language file found !");
            e.printStackTrace();
        }
    }

    public static String translate(String base)
    {
        if(translations.containsKey(base))
            return translations.get(base);
        else
        {
            Main.warn("No translation found for: " + base);
            return base;
        }
    }
}
