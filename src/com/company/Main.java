package com.company;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Main{

    static private boolean isDigit(String s) throws NumberFormatException {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static class FrequencyDictionary{
        private Map<String, Integer> map = new HashMap<String, Integer>();
        private int totalCounter = 0;

        private void FillMap(String text){
            // сначала переведём всё в нижний регистр чтобы слова не различались из-за регистров
            String text_LowerCase = text.toLowerCase();

            // теперь заменим все знаки .,<>(){}[];:|\/_-+='"`~!@#№$%^&?* на пробелы  и сразу удалим повторяющиеся пробелы
            String text_onlyLettersAndNumebers = text_LowerCase.replaceAll("[.,<>(){};:|+-/|_='\"`~!@#№$%^&?—*]"," ")
                    .replace('[', ' ')
                    .replace(']', ' ')
                    .replace('\\', ' ')
                    .replaceAll("\\s+", " ");

            // а сейчас разделим по пробелам на отдельные слова
            String[] words = text_onlyLettersAndNumebers.split("\\s+");

            // а теперь запишем всё в "map", откидывая числа без включения буковок
            for(String word: words){
                if (!isDigit(word) && !word.isEmpty()){
                    if (map.containsKey(word)){
                        int currentCount = map.get(word);
                        map.replace(word, currentCount + 1);
                    }
                    else{
                        map.put(word, 1);
                    }
                    totalCounter++;
                }
            }
            // а теперь сортируем "map" по ключу
            map = map.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        }
        public FrequencyDictionary(File file) throws IOException {
            String text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            FillMap(text);
        }
        public FrequencyDictionary(String text){
            FillMap(text);
        }
        public int GetNumberOfAllWords(){
            return totalCounter;
        }
        public int GetNumberOfUniqueWords(){
            return map.size();
        }
        public int GetWordCounter(String word){
            if (map.containsKey(word.toLowerCase())){
                return map.get(word);
            }
            else return 0;
        }
        public int PrintAllData(){
            System.out.println("Number of words: " + totalCounter + "\nList:\nWord - Counter/Total - Percent");
            map.forEach( (k, v)->System.out.println(k + " - " + v + "/" + totalCounter + " - " + (double)v * 100.0 / (double)totalCounter) );
            return 0;
        }
    }

    public static void main(String[] args) throws IOException {
        FrequencyDictionary fd = new FrequencyDictionary(new File("Пища богов.txt"));

        fd.PrintAllData();

        System.out.println();
        System.out.println("Всего слов: " + fd.GetNumberOfAllWords());
        System.out.println("Уникальных слов: " + fd.GetNumberOfUniqueWords());
        System.out.println("Количество слов \"я\": " + fd.GetWordCounter("я") + " (" + fd.GetWordCounter("я") * 100.0 / fd.GetNumberOfAllWords() + "%)");
        System.out.println("Количество слов \"быть\": " + fd.GetWordCounter("быть") + " (" + fd.GetWordCounter("быть") * 100.0 / fd.GetNumberOfAllWords() + "%)");
        System.out.println("Количество слов \"ололо\": " + fd.GetWordCounter("ололо") + " (" + fd.GetWordCounter("ололо") * 100.0 / fd.GetNumberOfAllWords() + "%)");
    }
}
