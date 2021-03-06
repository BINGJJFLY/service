package com.wjz.service.manager.assistant;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionFactoryBean;

import com.wjz.service.context.ApplicationContextHolder;
import com.wjz.service.manager.ManagerException;
import com.wjz.service.manager.select.page.BaseSelectByPageInfoService;

import tk.mybatis.mapper.common.Mapper;

/**
 * <b>批量操作助手</b>
 *
 * @author iss002
 *
 * @param <T>
 */
public abstract class BatchAssistant<T, M extends Mapper<T>> extends BaseSelectByPageInfoService<T, M> {

	public static final String INSERT_METHOD_NAME = ".insertSelective";
	public static final String UPDATE_METHOD_NAME = ".updateByPrimaryKeySelective";
	public static final String DELETE_METHOD_NAME = ".deleteByPrimaryKey";
	public static final String DEFAULT_SQLSESSIONFACTORY_NAME = "sqlSessionFactory";

	private final Object lock = new Object();
	private SqlSessionFactoryBean sqlSessionFactory;
	private String namespace;

	public BatchAssistant(M mapper) {
		super(mapper);
	}

	public SqlSessionFactoryBean getSqlSessionFactory() {
		synchronized (lock) {
			if (sqlSessionFactory == null) {
				sqlSessionFactory = (SqlSessionFactoryBean) ApplicationContextHolder
						.getBean(DEFAULT_SQLSESSIONFACTORY_NAME, SqlSessionFactoryBean.class);
			}
		}
		return sqlSessionFactory;
	}

	public void setSqlSessionFactory(SqlSessionFactoryBean sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}

	public void setSqlSessionFactory(String sqlSessionFactoryBeanName) {
		this.sqlSessionFactory = (SqlSessionFactoryBean) ApplicationContextHolder.getBean(sqlSessionFactoryBeanName,
				SqlSessionFactoryBean.class);
		if (sqlSessionFactory == null) {
			String message = "there is not a bean with the name is " + sqlSessionFactoryBeanName
					+ " of SqlSessionFactoryBean.";
			throw new ManagerException(message);
		}
	}

	public SqlSession createSqlSession() throws Exception {
		return getSqlSessionFactory().getObject().openSession(ExecutorType.BATCH, false);
	}

	public void commit(SqlSession sqlSession) {
		if (sqlSession != null) {
			// 并非事务提交（java.sql.Connection.commit();）
			// 执行批处理操作（java.sql.Statment.executeBatch();）
			sqlSession.commit();
			sqlSession.clearCache();
		}
	}

	public void rollback(SqlSession sqlSession) {
		if (sqlSession != null) {
			// 并非事务回滚（java.sql.Connection.rollback();）
			sqlSession.rollback();
		}
	}

	public void close(SqlSession sqlSession) {
		if (sqlSession != null) {
			sqlSession.close();
		}
	}

	public String getNamespace() {
		synchronized (lock) {
			if (namespace == null) {
				namespace = mapper.getClass().getName();
			}
		}
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
