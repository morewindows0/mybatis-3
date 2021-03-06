/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 */
public class DynamicSqlSource implements SqlSource {

  private final Configuration configuration;

  /**
   * 根SqlNode对象
   */
  private final SqlNode rootSqlNode;

  public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
    this.configuration = configuration;
    this.rootSqlNode = rootSqlNode;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    // 创建 DynamicContext
    DynamicContext context = new DynamicContext(configuration, parameterObject);
    // 解析sql片段，并将解析结果存储到DynamicContext中
    rootSqlNode.apply(context);
    // 创建SqlSourceBuilder对象
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
    // 解析出SqlSource对象
    /*
     * 构建 StaticSqlSource，在此过程中将 sql 语句中的占位符 #{} 替换为问号 ?，
     * 并为每个占位符构建相应的 ParameterMapping
     */
    SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
    // 获得BoundSql对象
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    // 添加附加参数到BoundSql对象中
    context.getBindings().forEach(boundSql::setAdditionalParameter);
    // 返回BoundSql对象
    return boundSql;
  }

}
