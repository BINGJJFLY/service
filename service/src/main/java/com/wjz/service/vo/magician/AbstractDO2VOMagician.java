package com.wjz.service.vo.magician;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.annotation.AnnotationUtils;

import com.wjz.service.anno.ViewObject;
import com.wjz.service.exception.UnAssignableException;
import com.wjz.service.vo.handler.Converter;
import com.wjz.service.vo.handler.PropertiesHandler;
import com.wjz.service.vo.handler.PropertiesHandlerRegistry;
import com.wjz.service.vo.manager.DefaultMetaObjectManager;
import com.wjz.service.vo.manager.MetaObjectManager;

/**
 * <b>基础Domain对象转换View对象转换器 </b>
 * 
 * @author iss002
 *
 */
public abstract class AbstractDO2VOMagician implements DO2VOMagician {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private PropertiesHandlerRegistry registy;

	public AbstractDO2VOMagician(PropertiesHandlerRegistry registy) {
		this.registy = registy;
	}

	public DefaultMetaObjectManager buildMetaObjectManager(Object target) {
		return new DefaultMetaObjectManager(target);
	}

	@Override
	public void setRegisty(PropertiesHandlerRegistry registy) {
		this.registy = registy;
	}

	protected void doConvert(MetaObjectManager metaDomainManager, MetaObjectManager metaViewManager) {
		Field[] fields = metaDomainManager.getDeclaredFields();
		if (!ArrayUtils.isEmpty(fields)) {
			for (Field field : fields) {
				if (!Modifier.isStatic(field.getModifiers())) {
					try {
						PropertiesHandler handler = registy.getHandler(field.getType());
						if (handler != null) {
							handler.handle(field, metaDomainManager.getMetaObject(), metaViewManager.getMetaObject(),
									new Converter() {

										@Override
										public Object convert(DO2VOMagician magician, Object target)
												throws UnAssignableException {
											if (SystemMetaObject.forObject(magician).getValue("registy") == null) {
												magician.setRegisty(registy);
											}
											return magician.do2vo(target);
										}

									});
						}
					} catch (UnAssignableException e) {
						log.error("An exception occurs when domain object [" + metaDomainManager.getMetaObject()
								+ "] converting to view object.", e);
						throw e;
					}
				}
			}
		}
	}

	@Override
	public Object do2vo(Object domain) {
		if (domain != null) {
			ViewObject viewObjectAnno = AnnotationUtils.getAnnotation(domain.getClass(), ViewObject.class);
			if (viewObjectAnno != null) {
				Class<?>[] viewTypes = viewObjectAnno.value();
				if (!ArrayUtils.isEmpty(viewTypes)) {
					return do2vo(domain, viewTypes[0]);
				}
			}
		}
		return domain;
	}

	@Override
	public Object do2vo(Object domain, Class<?> viewType) {
		if (domain != null) {
			Object view = ReflectUtils.newInstance(viewType);
			doConvert(buildMetaObjectManager(domain), buildMetaObjectManager(view));
			return view;
		}
		return domain;
	}
}
