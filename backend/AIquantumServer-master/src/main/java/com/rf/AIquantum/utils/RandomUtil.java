package com.rf.AIquantum.utils;

//import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 随机数工具类
 */
public class RandomUtil {

    /**
     * 获取两数之间的随机数
     * @param min 最小数
     * @param max 最大数
     * @return
     */
    public static int scopeNum(int min,int max){
        Random random = new Random();
        return random.nextInt(max)%(max-min+1) + min;
    }

    /**
     * 获取两数之间的随机数，排除指定的数据
     * @param min 最小值
     * @param max 最大值
     * @param arr 要排除的集合数据
     * @return
     */
    public static int chooseNum(int min, int max, List arr){
        //Log.i("集合",arr.toString());
        if ( arr.size() == 4){
            return 5;
        }
        Random random = new Random();
        int num = random.nextInt(max)%(max-min+1) + min;
        if ( arr.contains(num)){
            return chooseNum(min,max,arr);
        }else {
            return num;
        }
    }

    /**
     * 随机生成26位字母
     * @param size 生成位数
     * @param flag 大小写
     * @return
     */
    public static String randomChar(int size,char flag){
        //最终生成的字符串
        String str = "";
        for (int i = 0; i < size; i++) {
            str = str + (char)(Math.random()*26+flag);
        }
        return str;
    }


    /**
     * 获取到记忆量表字符出厂顺序
     *
     * @return
     */
    public static Map recallChar(){
        Map map = new HashMap();
        Map exerciseList = new HashMap();
        Map examList = new HashMap();

        Map mapChar0 = createBack0();
        Map mapChar1 = createBack(1);
        Map mapChar2 = createBack(2);
        String[] strings0 = new String[18];
        String[] strings1 = new String[18];
        String[] strings2 = new String[18];
        for ( int i = 0 ; i<mapChar0.size() ;i++){
            Object a = mapChar0.get(i);
            strings0[i] = a.toString();
            Object b = mapChar1.get(i);
            strings1[i] = b.toString();
            Object c = mapChar2.get(i);
            strings2[i] = c.toString();
        }
        exerciseList.put("back0",strings0);
        exerciseList.put("back1",strings1);
        exerciseList.put("back2",strings2);

        String[][] stringsa = new String[6][18];
        String[][] stringsb = new String[6][18];
        String[][] stringsc = new String[6][18];
        for ( int j = 0 ; j<6 ;j++){
            Map mapChar01 = createBack0();
            Map mapChar11 = createBack(1);
            Map mapChar21 = createBack(2);
            String[] strings01 = new String[18];
            String[] strings11 = new String[18];
            String[] strings21 = new String[18];
            for ( int i = 0 ; i<mapChar01.size() ;i++){
                Object a = mapChar01.get(i);
                strings01[i] = a.toString();
                Object b = mapChar11.get(i);
                strings11[i] = b.toString();
                Object c = mapChar21.get(i);
                strings21[i] = c.toString();
            }
            stringsa[j] = strings01;
            stringsb[j] = strings11;
            stringsc[j] = strings21;
        }
        examList.put("back0",stringsa);
        examList.put("back1",stringsb);
        examList.put("back2",stringsc);

        map.put("exerciseList",exerciseList);
        map.put("examList",examList);
        return map;
    }


    /**
     * 制作0back组块数据
     * @return
     */
    public static Map createBack0(){
        List list = new ArrayList();
        List indexList = new ArrayList();
        Map mapChar = new LinkedHashMap();
        mapChar = getMapChar(mapChar,list);
        indexList = getRandomNum(1,17,indexList,9);
        for ( Object index : indexList){
            mapChar.put(index,mapChar.get(0));
        }
        //Log.i("0-back",mapChar.toString());
        return mapChar;
    }

    /**
     * 1-back
     * @return
     */
    public static Map createBack(int type){
        Map mapChar = new LinkedHashMap();
        List list = new ArrayList();
        mapChar = getMapChar(mapChar,list);
        //Log.i(type+"-mapOld",mapChar.toString());
        List indexList = new ArrayList();
        indexList = getRandomNum(type,17,indexList,9);
        Collections.sort(indexList);
        for ( Object index : indexList){
            mapChar.put(index,mapChar.get((int)index-type));
        }
        //Log.i(type+"-Index",indexList.toString());
        //Log.i(type+"-back",mapChar.toString());
        return mapChar;
    }



    /**
     * 获取到18位的随机字符
     * @param map
     * @param list
     * @return
     */
    public static Map getMapChar(Map map,List list){
        char strChar = (char)(Math.random()*26+'A');
        if ( list.isEmpty()){
            list.add(strChar);
        }else {
            if ( !list.contains( strChar)){
                list.add(strChar);
            }
        }
        if ( list.size() == 18 ){
            for (int i = 0; i<list.size();i++){
                map.put(i,list.get(i));
            }
            return map;
        }else {
            return getMapChar(map,list);
        }
    }

    /**
     * 获取到范围内随机数
     * @param min
     * @param max
     * @param list
     * @return
     */
    public static List getRandomNum(int min,int max,List list,int size){
        Random random = new Random();
        int num = random.nextInt(max)%(max-min+1) + min;
        if ( !list.contains(num)) {
            list.add(num);
        }
        if ( list.size() == size){
            return list;
        }else {
            return getRandomNum(min,max,list,size);
        }
    }

    /**
     * 获取到记忆量表字符出厂顺序2
     * @return
     */
    public static Map recallCharTwo(){
        Map map = new HashMap();
        Map exerciseList = new HashMap();
        Map examList = new HashMap();

        Map back1 = new LinkedHashMap();
        Map back1Map1 = createBackOne(1);
        Map back1Map2 = createBackTwo(1,back1Map1);
        for ( int i = 0 ; i<back1Map1.size() ;i++){
            back1.put(i,back1Map1.get(i));
        }
        for ( int i = 0 ; i<back1Map2.size() ;i++){
            back1.put(i+9,back1Map2.get(i));
        }
        Map back2 = new LinkedHashMap();
        Map back2Map1 = createBackOne(2);
        Map back2Map2 = createBackS(2,back2Map1);
        for ( int i = 0 ; i<back2Map1.size() ;i++){
            back2.put(i,back2Map1.get(i));
        }
        for ( int i = 0 ; i<back2Map2.size() ;i++){
            back2.put(i+9,back2Map2.get(i));
        }
        //System.out.println("mapChar1:"+mapChar1);
        //System.out.println("mapChar2:"+mapChar2);
        String[] strings1 = new String[18];
        String[] strings2 = new String[18];
        for ( int i = 0 ; i<back1.size() ;i++){
            Object b = back1.get(i);
            strings1[i] = b.toString();
            Object c = back2.get(i);
            strings2[i] = c.toString();
        }
        exerciseList.put("back1",strings1);
        exerciseList.put("back2",strings2);

        String[][] stringsb = new String[3][18];
        String[][] stringsc = new String[3][18];
        for ( int j = 0 ; j<3 ;j++){
            Map examback1 = new LinkedHashMap();
            Map examback1Map1 = createBackOne(1);
            Map examback1Map2 = createBackTwo(1,examback1Map1);
            for ( int i = 0 ; i<examback1Map1.size() ;i++){
                examback1.put(i,examback1Map1.get(i));
            }
            for ( int i = 0 ; i<examback1Map2.size() ;i++){
                examback1.put(i+9,examback1Map2.get(i));
            }
            Map examback2 = new LinkedHashMap();
            Map examback2Map1 = createBackOne(2);
            Map examback2Map2 = createBackS(2,examback2Map1);
            for ( int i = 0 ; i<examback2Map1.size() ;i++){
                examback2.put(i,examback2Map1.get(i));
            }
            for ( int i = 0 ; i<examback2Map2.size() ;i++){
                examback2.put(i+9,examback2Map2.get(i));
            }
            String[] strings11 = new String[18];
            String[] strings21 = new String[18];
            for ( int i = 0 ; i<examback1.size() ;i++){
                Object b = examback1.get(i);
                strings11[i] = b.toString();
                Object c = examback2.get(i);
                strings21[i] = c.toString();
            }
            stringsb[j] = strings11;
            stringsc[j] = strings21;
        }
        examList.put("back1",stringsb);
        examList.put("back2",stringsc);

        map.put("exerciseList",exerciseList);
        map.put("examList",examList);
        return map;
    }

    /**
     * 1-back
     * @return
     */
    public static Map createBackOne(int type){
        Map mapChar = new LinkedHashMap();
        List list = new ArrayList();
        mapChar = getMapCharTwo(mapChar,list);
        List indexList = new ArrayList();
        indexList = getRandomNumTwo(type,8,indexList,4);
        Collections.sort(indexList);
        for ( Object index : indexList){
            mapChar.put(index,mapChar.get((int)index-type));
        }
        return mapChar;
    }

    /**
     * 1-back
     * @return
     */
    public static Map createBackTwo(int type,Map map11){
        Map mapChar = new LinkedHashMap();
        List list = new ArrayList();
        list.add(map11.get(8));
        mapChar = getMapCharTwo(mapChar,list);
        List indexList = new ArrayList();
        indexList = getRandomNumTwo(type,8,indexList,4);
        Collections.sort(indexList);
        for ( Object index : indexList){
            mapChar.put(index,mapChar.get((int)index-type));
        }
        return mapChar;
    }

    /**
     * 1-back
     * @return
     */
    public static Map createBackS(int type,Map map11){
        Map mapChar = new LinkedHashMap();
        List list = new ArrayList();
        list.add(map11.get(8));
        mapChar = getMapCharTwo(mapChar,list);
        List indexList = new ArrayList();
        indexList = getRandomNumTwo(type,8,indexList,5);
        Collections.sort(indexList);
        for ( Object index : indexList){
            mapChar.put(index,mapChar.get((int)index-type));
        }
        return mapChar;
    }



    /**
     * 获取到18位的随机字符
     * @param map
     * @param list
     * @return
     */
    public static Map getMapCharTwo(Map map,List list){
        //char strChar = (char)(Math.random()*26+'A');
        int ran1 = (int) (Math.random()*(10-1)+1);
        if ( list.isEmpty()){
            list.add(ran1);
        }else {
            if ( !list.contains( ran1)){
                list.add(ran1);
            }
        }
        if ( list.size() == 9 ){
            for (int i = 0; i<list.size();i++){
                map.put(i,list.get(i));
            }
            return map;
        }else {
            return getMapCharTwo(map,list);
        }
    }

    /**
     * 获取到范围内随机数
     * @param min
     * @param max
     * @param list
     * @return
     */
    public static List getRandomNumTwo(int min,int max,List list,int size){
        Random random = new Random();
        int num = random.nextInt(max)%(max-min+1) + min;
        if ( !list.contains(num)) {
            list.add(num);
        }
        if ( list.size() == size){
            return list;
        }else {
            return getRandomNumTwo(min,max,list,size);
        }
    }


    /*public static Map recallCharTwo(){
        Map map = new HashMap();
        Map exerciseList = new HashMap();
        Map examList = new HashMap();

        String[] strings1 = new String[20];
        String[] strings2 = new String[20];
        for ( int i = 0 ; i<20 ;i++){
            int ran1 = (int) (Math.random()*(10-1)+1);
            int ran2 = (int) (Math.random()*(10-1)+1);
            strings1[i] = String.valueOf(ran1);
            strings2[i] = String.valueOf(ran2);
        }
        exerciseList.put("back1",strings1);
        exerciseList.put("back2",strings2);

        String[][] stringsb = new String[3][20];
        String[][] stringsc = new String[3][20];
        for ( int j = 0 ; j<3 ;j++){
            String[] strings11 = new String[20];
            String[] strings21 = new String[20];
            for ( int i = 0 ; i<20 ;i++){
                int ran1 = (int) (Math.random()*(10-1)+1);
                int ran2 = (int) (Math.random()*(10-1)+1);
                strings11[i] = String.valueOf(ran1);
                strings21[i] = String.valueOf(ran2);
            }
            stringsb[j] = strings11;
            stringsc[j] = strings21;
        }
        examList.put("back1",stringsb);
        examList.put("back2",stringsc);

        map.put("exerciseList",exerciseList);
        map.put("examList",examList);
        return map;
    }*/


}
