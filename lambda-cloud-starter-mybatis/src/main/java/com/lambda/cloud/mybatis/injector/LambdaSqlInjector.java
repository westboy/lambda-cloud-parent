package com.lambda.cloud.mybatis.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.methods.*;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.lambda.cloud.mybatis.injector.method.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        Stream.Builder<AbstractMethod> builder = Stream.<AbstractMethod>builder()
                .add(new Insert())
                .add(new Delete())
                .add(new Update())
                .add(new Exists())
                .add(new SelectCount())
                .add(new SelectMaps())
                .add(new SelectObjs())
                .add(new SelectList())
                .add(new InsertAll());
        if (tableInfo.havePK()) {
            builder.add(new DeleteById())
                    .add(new DeleteByIds())
                    .add(new UpdateById())
                    .add(new SelectById())
                    .add(new SelectByIds());
        }
        Optional<TableFieldInfo> optional = getCodeField(tableInfo);
        optional.ifPresent(codeField -> builder.add(new SelectByCode(codeField))
                .add(new UpdateByCode(codeField))
                .add(new DeleteByCode(codeField)));
        return builder.build().collect(Collectors.toList());
    }

    private Optional<TableFieldInfo> getCodeField(TableInfo tableInfo) {
        return tableInfo.getFieldList().stream()
                .filter(e -> null != e.getField().getAnnotation(TableCodeField.class))
                .findFirst();
    }
}
