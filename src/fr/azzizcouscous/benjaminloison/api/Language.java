package fr.azzizcouscous.benjaminloison.api;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import fr.azzizcouscous.benjaminloison.main.Main;

public class Language
{
    private static String example = "// <-- Use this to do a comment\n" + "// Find here the color codes: http://j.gs/9KIx" + "\n§4Something not translated=§2Something translated";
    static Map<String, String> translations = new HashMap<String, String>();

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
            Scanner scan = new Scanner(FileAPI.languageFile, "UTF-8");
            int lineNumber = 0;
            while(scan.hasNextLine())
            {
                String line = scan.nextLine(), parts[] = line.split("=");
                lineNumber++;
                if(line.startsWith("//"))
                    continue;
                if(parts.length != 2)
                    Main.warn(translate("Invalid translation in line (") + lineNumber + translate("): ") + line + translate(" | Must be like this: ") + example);
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
        return base;
    }
}
