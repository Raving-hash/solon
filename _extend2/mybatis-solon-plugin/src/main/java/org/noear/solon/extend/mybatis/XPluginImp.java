package org.noear.solon.extend.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.noear.solon.XApp;
import org.noear.solon.XUtil;
import org.noear.solon.core.*;

public class XPluginImp implements XPlugin {
    @Override
    public void start(XApp app) {

        Aop.factory().beanCreatorAdd(MapperScan.class, (clz0, wrap, anno) -> {
            String dir = anno.basePackages().replace('.', '/');
            String sessionFactoryRef = anno.sqlSessionFactoryRef();

            Aop.getAsyn(sessionFactoryRef, (bw -> {
                if (bw.raw() instanceof SqlSessionFactory) {
                    scanMapper(dir, bw.raw());
                }
            }));
        });

        Aop.factory().beanInjectorAdd(Df.class, (varH, anno)->{
            Aop.getAsyn(anno.value(),(bw)->{
                if(bw.raw() instanceof SqlSessionFactory) {
                    SqlSessionFactory factory = bw.raw();

                    if(varH.getType().isInterface()){
                        Object mapper = factory.openSession().getMapper(varH.getType());

                        varH.setValue(mapper);
                        return;
                    }

                    if(SqlSession.class.isAssignableFrom(varH.getType())){
                        varH.setValue(factory.openSession());
                        return;
                    }

                    if(SqlSessionFactory.class.isAssignableFrom(varH.getType())){
                        varH.setValue(factory);
                        return;
                    }

                }
            });
        });
    }

    private void scanMapper(String dir, SqlSessionFactory factory) {
        XScaner.scan(dir, n -> n.endsWith(".class"))
                .stream()
                .map(name -> {
                    String className = name.substring(0, name.length() - 6);
                    return XUtil.loadClass(className.replace("/", "."));
                })
                .forEach((clz) -> {
                    if (clz != null && clz.isInterface()) {
                        Object mapper = factory.openSession().getMapper(clz);

                        Aop.put(clz, mapper);
                    }
                });
    }
}
