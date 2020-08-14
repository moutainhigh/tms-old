if (object_id('ts_pay_detail_update', 'TR') is not null)
    drop trigger ts_pay_detail_update
go
create trigger ts_pay_detail_update
on ts_pay_detail
    after update
as
    declare @pk_pay_detail varchar(50),
             @vbillstatus int,
             @newVbillstatus int,
             @ts char(19)
    --这里的inserted是是从一个状态到另一个状态的旧数据，比如vbillstatus从0-41，那么为0的记录存储在deleted，为41的记录存储在inserted
    select @pk_pay_detail = pk_pay_detail,@vbillstatus=vbillstatus,@ts=ts from deleted
    select @newVbillstatus=vbillstatus from inserted
    --print '------旧状态'+convert(varchar,@vbillstatus)+'----'
    --print '------新状态'+convert(varchar,@newVbillstatus)+'----'
    if((@vbillstatus=41 and @newVbillstatus=0)) --从0-41，确认 ,从41-0，反确认 
        begin
           --print '=======确认或者反确认========'
            update ts_pay_detail set def1='已做反确认',def2=ts where pk_pay_detail=@pk_pay_detail
        end
    else if((@vbillstatus=45 and @newVbillstatus=0)) --从45-0，反提交
        begin
           --print '=======确认或者反确认========'
            update ts_pay_detail set def1='已做反提交',def2=ts where pk_pay_detail=@pk_pay_detail
        end
    else
        begin
            --print '============'
            update ts_pay_detail set def1=null,def2=null where pk_pay_detail=@pk_pay_detail
        end
        
go

if (object_id('ts_receive_detail_update', 'TR') is not null)
    drop trigger ts_receive_detail_update
go
create trigger ts_receive_detail_update
on ts_receive_detail
    after update
as
    declare @pk_receive_detail varchar(50),
             @vbillstatus int,
             @newVbillstatus int,
             @ts char(19)
    --这里的inserted是是从一个状态到另一个状态的旧数据，比如vbillstatus从0-41，那么为0的记录存储在deleted，为41的记录存储在inserted
    select @pk_receive_detail = pk_receive_detail,@vbillstatus=vbillstatus,@ts=ts from deleted
    select @newVbillstatus=vbillstatus from inserted
    --print '------旧状态'+convert(varchar,@vbillstatus)+'----'
    --print '------新状态'+convert(varchar,@newVbillstatus)+'----'
    if((@vbillstatus=31 and @newVbillstatus=0)) --从0-41，确认 ,从41-0，反确认 
        begin
           --print '=======确认或者反确认========'
            update ts_receive_detail set def1='已做反确认',def2=ts where pk_receive_detail=@pk_receive_detail
        end
    else if((@vbillstatus=35 and @newVbillstatus=0)) --从45-0，反提交
        begin
           --print '=======确认或者反确认========'
            update ts_receive_detail set def1='已做反提交',def2=ts where pk_receive_detail=@pk_receive_detail
        end
    else
        begin
            --print '============'
            update ts_receive_detail set def1=null,def2=null where pk_receive_detail=@pk_receive_detail
        end



