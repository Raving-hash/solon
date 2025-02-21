package org.noear.solon.cloud.extend.xxljob;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.cloud.CloudManager;
import org.noear.solon.cloud.CloudProps;
import org.noear.solon.cloud.extend.xxljob.service.CloudJobServiceImpl;
import org.noear.solon.cloud.impl.CloudJobBeanBuilder;
import org.noear.solon.core.AopContext;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.event.AppLoadEndEvent;

/**
 * @author noear
 * @since 1.4
 */
public class XPluginImp implements Plugin {
    @Override
    public void start(AopContext context) {
        CloudProps cloudProps = new CloudProps(context, "xxljob");

        if (Utils.isEmpty(cloudProps.getServer())) {
            return;
        }

        if (cloudProps.getJobEnable() == false) {
            return;
        }

        //注册 bean 给 XxlJobAutoConfig 用
        BeanWrap beanWrap = context.wrap("xxljob-cloudProps", cloudProps);
        context.putWrap("xxljob-cloudProps", beanWrap);

        ///////////////////////////////////////////////////

        //注册Job服务
        CloudManager.register(new CloudJobServiceImpl());

        //添加 @CloudJob 对 IJobHandler 类的支持 //@since 2.0
        CloudJobBeanBuilder.getInstance().addBuilder(IJobHandler.class,(clz, bw, anno) -> {
            //支持${xxx}配置
            String name = Solon.cfg().getByParse(Utils.annoAlias(anno.value(), anno.name()));
            //提示：不支持CloudJob拦截器
            XxlJobExecutor.registJobHandler(name, bw.raw());
        });

        //注册构建器和提取器
        context.beanExtractorAdd(XxlJob.class, new XxlJobExtractor());

        //构建自动配置
        context.beanMake(XxlJobAutoConfig.class);

        Solon.app().onEvent(AppLoadEndEvent.class, e -> {
            XxlJobExecutor executor = context.getBean(XxlJobExecutor.class);
            executor.start();
        });
    }
}
