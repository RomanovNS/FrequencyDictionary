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
import java.util.*;
import java.util.concurrent.*;
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
        private Map<String, Integer> dictionary = new HashMap<String, Integer>();
        private int totalCounter = 0;
        private String wholeText;

        public static class DictionaryMaker implements Callable<Map<String, Integer>> {
            private String localText;

            private Map<String, Integer> makeDictionary(String text){
                Map<String, Integer> dictionaryLocal = new HashMap<String, Integer>();

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

                // а теперь запишем всё в dictionary, откидывая числа без включения буковок
                for(String word: words){
                    if (!isDigit(word) && !word.isEmpty()){
                        if (dictionaryLocal.containsKey(word)){
                            int currentCount = dictionaryLocal.get(word);
                            dictionaryLocal.replace(word, currentCount + 1);
                        }
                        else{
                            dictionaryLocal.put(word, 1);
                        }
                    }
                }
                // а теперь сортируем dictionary по ключу
                dictionaryLocal = dictionaryLocal.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

                return dictionaryLocal;
            }
            public DictionaryMaker(String textPart){
                localText = textPart;
            }
            @Override
            public Map<String, Integer> call() throws Exception {
                return makeDictionary(localText);
            }
        }

        void CreateDictionary(int threadsNum){
            ExecutorService executor = Executors.newCachedThreadPool();
            List<Future<Map<String, Integer>>> futureLocalDictionaries = new ArrayList<Future<Map<String, Integer>>>();

            // делим текст на равные (ну почти) кусочки так, чтобы не разрывать слова
            String[] textParts = new String[threadsNum];
            int textLength = wholeText.length();
            int substringStart = 0;
            int substringEnd = 0;
            int partMidLen = textLength / threadsNum;
            for(int i = 0; i < threadsNum; i++){
                // задаём начало подстроки
                substringStart = substringEnd;
                // задаём конец подстроки
                substringEnd = (substringStart + partMidLen < textLength)? substringStart + partMidLen : textLength - 1;
                while (wholeText.charAt(substringEnd) != ' ' && substringEnd < (textLength - 1))
                    substringEnd++;
                // вырезаем кусок текста в подстроку
                textParts[i] = wholeText.substring(substringStart, substringEnd);
            }

            // вызываем потоки и объединяем результаты их работы (локальные библиотеки)
            try{
                // вызываем потоки
                for(int i = 0; i < threadsNum; i++){
                    futureLocalDictionaries.add(executor.submit(new DictionaryMaker(textParts[i])));
                }
                // объединяем результаты
                for(int i = 0; i < threadsNum; i++){
                    Map<String, Integer> localDictionary = futureLocalDictionaries.get(i).get();
                    localDictionary.forEach( (k, v) -> dictionary.merge(k, v, (prevCount, count) -> prevCount + count));
                }
                // считаем общее количество слов
                dictionary.forEach( (k, v) -> totalCounter += v);
            }
            catch (InterruptedException | ExecutionException exception){
                System.out.println("Что-то пошло не так при вызове потоков в FrequencyDictionary!");
                System.exit(666);
            }
            finally {
                executor.shutdown();
            }
        }

        public FrequencyDictionary(File file, int threads) throws IOException {
            wholeText = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            CreateDictionary(threads);
        }
        public FrequencyDictionary(String text, int threads){
            wholeText = text;
            CreateDictionary(threads);
        }
        public int GetNumberOfAllWords(){
            return totalCounter;
        }
        public int GetNumberOfUniqueWords(){
            return dictionary.size();
        }
        public int GetWordCounter(String word){
            if (dictionary.containsKey(word.toLowerCase())){
                return dictionary.get(word);
            }
            else return 0;
        }
        public int PrintAllData(){
            System.out.println("Number of words: " + totalCounter + "\nList:\nWord - Counter/Total - Percent");
            dictionary.forEach( (k, v)->System.out.println(k + " - " + v + "/" + totalCounter + " - " + (double)v * 100.0 / (double)totalCounter) );
            return 0;
        }
    }

    public static void main(String[] args) throws IOException {
        int threads = 4;
        long startTime = System.nanoTime();
        FrequencyDictionary fd = new FrequencyDictionary(new File("test book ''Пища богов''.txt"), threads);
        long executionTime = System.nanoTime() - startTime;

        fd.PrintAllData();
        System.out.println();
        System.out.println("Execution time: " + executionTime / 1000000000.0 + " sec");
        System.out.println();

        System.out.println("Всего слов: " + fd.GetNumberOfAllWords());
        System.out.println("Уникальных слов: " + fd.GetNumberOfUniqueWords());
        System.out.println("Количество слов \"я\": " + fd.GetWordCounter("я") + " (" + fd.GetWordCounter("я") * 100.0 / fd.GetNumberOfAllWords() + "%)");
        System.out.println("Количество слов \"быть\": " + fd.GetWordCounter("быть") + " (" + fd.GetWordCounter("быть") * 100.0 / fd.GetNumberOfAllWords() + "%)");
        System.out.println("Количество слов \"ололо\": " + fd.GetWordCounter("ололо") + " (" + fd.GetWordCounter("ололо") * 100.0 / fd.GetNumberOfAllWords() + "%)");

        //byte[] input = new byte[100];
        //System.in.read(input);
        //String str = new String(input);
        //System.out.println();
        //System.out.println(str);

    }
}
