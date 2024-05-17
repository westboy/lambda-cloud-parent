
package com.jingfang.cloud.lucene.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.jingfang.cloud.lucene.annotation.LuceneField;
import com.jingfang.cloud.lucene.model.IndexObject;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.highlight.Highlighter;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DocumentUtil
 *
 * @author Jin
 */
public class IndexObjectUtil {

    /**
     * @param indexObject
     * @return
     */
    public static Document indexObjectToDocument(IndexObject indexObject) {
        Document doc = new Document();
        Field[] fields = ReflectUtil.getFields(indexObject.getClass());
        Arrays.stream(fields).forEach(field -> {
            LuceneField luceneField = getAnnotation(field);
            if (ObjectUtil.isNotNull(luceneField)) {
                //获取注解上的字段名
                String docFieldName = getLuceneFieldName(field);
                //获取Object字段值
                Object value = ReflectUtil.getFieldValue(indexObject, field.getName());
                //字段类型目前只能是TextField 和 StoredField
                if (ObjectUtil.equal(luceneField.field(), TextField.class) || luceneField.isExtendField()) {
                    if (ObjectUtil.isNotNull(value)) {
                        //是否为扩展字段
                        if (luceneField.isExtendField()) {
                            //扩展字段类型必须是map
                            if (ObjectUtil.equal(field.getType(), Map.class)) {
                                Map<String, Object> extendFieldMap = (Map<String, Object>) value;
                                if (ObjectUtil.isNotNull(extendFieldMap)) {
                                    extendFieldMap.forEach((extFieldName, extFieldValue) -> {
                                        TextField textField = new TextField(extFieldName, (String) extFieldValue, luceneField.store());
                                        doc.add(textField);
                                    });
                                }
                            }
                        } else {
                            TextField textField = new TextField(docFieldName, (String) value, luceneField.store());
                            doc.add(textField);
                        }
                    }
                } else if (ObjectUtil.equal(luceneField.field(), StoredField.class)) {
                    if (ObjectUtil.isNotNull(value)) {
                        if (value instanceof Integer) {
                            StoredField storedField = new StoredField(docFieldName, (Integer) value);
                            doc.add(storedField);
                        } else if (value instanceof Long) {
                            StoredField storedField = new StoredField(docFieldName, (Long) value);
                            doc.add(storedField);
                        } else if (value instanceof Float) {
                            StoredField storedField = new StoredField(docFieldName, (Float) value);
                            doc.add(storedField);
                        } else if (value instanceof Double) {
                            StoredField storedField = new StoredField(docFieldName, (Double) value);
                            doc.add(storedField);
                        } else {
                            StoredField storedField = new StoredField(docFieldName, (String) value);
                            doc.add(storedField);
                        }
                    }

                }
            }
        });
        return doc;
    }


    /**
     * @param analyzer
     * @param highlighter
     * @param doc
     * @param score
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends IndexObject> T documentToIndexObject(Analyzer analyzer, Highlighter highlighter, Document doc, float score, Class<T> clazz) {
        T obj = ReflectUtil.newInstance(clazz);
        Field[] fields = ReflectUtil.getFields(clazz);
        obj.setScore(score);
        Arrays.stream(fields).forEach(field -> {
            LuceneField luceneField = getAnnotation(field);
            if (ObjectUtil.isNotNull(luceneField)) {
                //获取注解上的字段名
                String docFieldName = getLuceneFieldName(field);
                if (ObjectUtil.equal(luceneField.field(), TextField.class) && luceneField.isQueryField()) {
                    //是否查询字段 用于关键字加亮
                    if (luceneField.isExtendField()) {
                        //扩展字段属性必须是Map类型
                        if (ObjectUtil.equal(field.getType(), Map.class)) {
                            //获取字段名用于下面判断排除
                            List<String> objectFileNames = Arrays.stream(fields).map(IndexObjectUtil::getLuceneFieldName).collect(Collectors.toList());
                            //扩展字段map
                            Map<String, Object> extendFieldMap = new HashMap<>();
                            //遍历取出扩展字段的值
                            doc.getFields().parallelStream().forEach(indexField -> {
                                //排除
                                if (objectFileNames.parallelStream().noneMatch(s -> ObjectUtil.equal(indexField.name(), s))) {
                                    //自定义关键字加亮
                                    extendFieldMap.put(indexField.name(), indexField.stringValue());
                                }
                            });
                            //设置扩展字段的值
                            ReflectUtil.setFieldValue(obj, field.getName(), extendFieldMap);
                        }
                    } else {
                        //设置并加亮
                        ReflectUtil.setFieldValue(obj, field.getName(), highlighter(analyzer, highlighter, doc, docFieldName));
                    }
                } else {
                    ReflectUtil.setFieldValue(obj, field.getName(), doc.get(docFieldName));
                }
            }
        });
        return obj;
    }

    /**
     * 获取字段名
     *
     * @param field
     * @return
     */
    private static String getLuceneFieldName(Field field) {
        LuceneField luceneField = getAnnotation(field);
        String docFieldName = null;
        if (ObjectUtil.isNotNull(luceneField)) {
            docFieldName = luceneField.name();
            if (ObjectUtil.isEmpty(docFieldName))
                docFieldName = field.getName();
        }
        return docFieldName;
    }

    /**
     * 获取需要检索的字段名称
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends IndexObject> String[] getQueryFieldNames(Class<T> clazz) {
        Field[] fields = ReflectUtil.getFields(clazz);
        List<String> queryFieldNames = new ArrayList<>();
        Arrays.stream(fields).parallel().forEach(field -> {
            LuceneField luceneField = getAnnotation(field);
            if (luceneField.isQueryField()) {
                queryFieldNames.add(getLuceneFieldName(field));
            }
        });
        return queryFieldNames.toArray(new String[0]);
    }

    /**
     * 获取注解
     *
     * @param field
     * @return
     */
    private static LuceneField getAnnotation(Field field) {
        LuceneField luceneField = field.getAnnotation(LuceneField.class);
        //字段上没有从方法上获取
        if (ObjectUtil.isNull(luceneField)) {
            String getMethodName = "get" + StrUtil.upperFirst(field.getName());
            Method method = ReflectUtil.getPublicMethod(field.getDeclaringClass(), getMethodName);
            luceneField = method.getAnnotation(LuceneField.class);
        }
        return luceneField;
    }

    /**
     * 加量
     *
     * @param analyzer
     * @param highlighter
     * @param document
     * @param field
     * @return
     */
    @SneakyThrows
    private static String highlighter(Analyzer analyzer, Highlighter highlighter, Document document, String field) {
        String fieldValue = document.get(field);
        if (fieldValue != null) {
            TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(fieldValue));
            fieldValue = highlighter.getBestFragment(tokenStream, fieldValue);
        }
        return fieldValue;
    }

}
