package com.lambda.cloud.mybatis.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.methods.*;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.lambda.cloud.mybatis.injector.method.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.session.Configuration;

/**
 * 添加自定义方法
 *
 * @author jpjoo
 */
public class LambdaSqlInjector extends DefaultSqlInjector {

    /**
     * 如果只需增加方法，保留MP自带方法
     * 可以super.getMethodList() 再add
     *
     * @return List<AbstractMethod>
     */
    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration, Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = new ArrayList<>(super.getMethodList(configuration, mapperClass, tableInfo));
        methodList.add(new Exists());
        methodList.add(new InsertAll());
        Optional<TableFieldInfo> optional = getCodeField(tableInfo);
        optional.ifPresent(codeField -> {
            methodList.add(new SelectByCode(codeField));
            methodList.add(new UpdateByCode(codeField));
            methodList.add(new DeleteByCode(codeField));
        });
        return methodList;
    }

    private Optional<TableFieldInfo> getCodeField(TableInfo tableInfo) {
        return tableInfo.getFieldList().stream()
                .filter(e -> null != e.getField().getAnnotation(TableCodeField.class))
                .findFirst();
    }
}
