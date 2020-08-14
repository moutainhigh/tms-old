--alter table ts_role add pk_corp varchar(50);
--alter table ts_dept alter column pk_corp varchar(50);
--ALTER TABLE ts_pay_detail DROP COLUMN pk_valuation_type

--1、spid:死锁的进程,tableName :死锁的表
--select request_session_id spid,OBJECT_NAME(resource_associated_entity_id)tableName from sys.dm_tran_locks where resource_type='OBJECT'
--2、spid:要结束的进程id
--kill spid （例如：kill 137）

--/////////////////////导出表insert语句的存储过程///////////////////////
--exec usp_GenInsertSql "nw_fun","isnull(dr,0)=0";
drop PROCEDURE usp_GenInsertSql;
 CREATE PROCEDURE usp_GenInsertSql 
@tablename VARCHAR(256),
@where varchar(1000)
AS
  BEGIN
      DECLARE @sql VARCHAR(MAX)
      DECLARE @sqlValues VARCHAR(MAX)

      SET @sql =' ('
      SET @sqlValues = 'values (''+'

      SELECT @sqlValues = @sqlValues + cols + ' + '','' + ',
             @sql = @sql + QUOTENAME(name) + ','
      FROM   (SELECT CASE
                       WHEN xtype IN ( 48, 52, 56, 59,
                                       60, 62, 104, 106,
                                       108, 122, 127 ) THEN
                       'case when ' + name
                       + ' is null then ''NULL'' else ' + 'cast('
                       + name + ' as varchar)' + ' end'
                       WHEN xtype IN ( 58, 61 ) THEN
                       'case when ' + name
                       +
                       ' is null then ''NULL'' else '
                                                     + ''''''''' + ' + 'cast(' +
                       name
                                                     +
                                                     ' as varchar)'
                                                     + '+''''''''' + ' end'
                       WHEN xtype = 167  THEN 'case when ' + name
                                                  +
                       ' is null then ''NULL'' else '
                                                  + ''''''''' + ' + 'replace(' +
                                                  name
                                                  + ','''''''','''''''''''')' +
                                                  '+'''''''''
                                                  + ' end'
                       WHEN xtype = 231  THEN 'case when ' + name
                                                  +
                       ' is null then ''NULL'' else '
                                                  + '''N'''''' + ' + 'replace('
                                                  +
                                                  name
                                                  + ','''''''','''''''''''')' +
                                                  '+'''''''''
                                                  + ' end'
                       WHEN xtype = 175  THEN
                       'case when ' + name
                       + ' is null then ''NULL'' else '
                       + ''''''''' + ' + 'cast(replace(' +
                       name
                       + ','''''''','''''''''''') as Char('
                       + Cast(length AS VARCHAR) +
                       '))+'''''''''
                       + ' end'
                       WHEN xtype = 239  THEN
                       'case when ' + name
                       + ' is null then ''NULL'' else '
                       + '''N'''''' + ' + 'cast(replace(' +
                       name
                       + ','''''''','''''''''''') as Char('
                       + Cast(length AS VARCHAR) +
                       '))+'''''''''
                       + ' end'
                       ELSE '''NULL'''
                     END AS Cols,
                     name
              FROM   syscolumns
              WHERE  id = Object_id(@tablename)) T

      SET @sql = REPLACE(@sql,'[','')
      SET @sql = REPLACE(@sql,']','')
      SET @sql ='select ''INSERT INTO ' + @tablename + ''
                + LEFT(@sql, Len(@sql)-1) + ') '
                + LEFT(@sqlValues, Len(@sqlValues)-4)
                + ')'' from ' + @tablename + ' where '+ @where + ';'
     print @sql
    exec (@sql)d
  END

go

