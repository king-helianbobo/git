package org.word.segment.test;

import java.io.File;

import org.soul.treeSplit.Forest;
import org.soul.treeSplit.LibraryToForest;
import org.soul.treeSplit.Value;
import org.soul.utility.UserDefineLibrary;


/**
 * 重新加载用户自定义辞典的两种方式
 * @author ansj
 *
 */
public class ReloadAmbiguityLibrary {
    public static void main(String[] args) throws Exception {
        //从文件中reload
        loadFormFile();
        //通过内存中reload
        loadFormStr();
        
        
        //歧义辞典增加新词
        
        Value value = new Value("三个和尚","三个","m","和尚","n") ;
        LibraryToForest.insertWord(UserDefineLibrary.ambiguityForest, value) ;
        
        
        //歧义辞典删除词
        LibraryToForest.removeWord(UserDefineLibrary.ambiguityForest, "三个和尚") ;
        
    }

    private static void loadFormStr() {
        // TODO Auto-generated method stub
        Forest forest = new Forest() ;
        
        Value value = new Value("三个和尚","三个","m","和尚","n") ;
        LibraryToForest.insertWord(forest, value) ;
      //将新构建的辞典树替换掉旧的。
        UserDefineLibrary.ambiguityForest = forest ;
    }

    private static void loadFormFile() throws Exception {
        // TODO Auto-generated method stub
        //make new forest
        Forest forest = LibraryToForest.makeForest("new_Library_Path") ;

        //将新构建的辞典树替换掉舊的。
        UserDefineLibrary.ambiguityForest = forest;
    }
}
