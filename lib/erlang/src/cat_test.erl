-module(cat_test).

-include("erlcat.hrl").
-export([event/0,trans/0,heart/0,init_context/0]).
-export([init/0,trans_count/1,heart_count/1,mutil_trans/0,l1/0]).

init()->
	erlcat:init_cat("testapp",#cat_config{enable_heartbeat=0,enable_debugLog=1,encoder_type=1}),
	ok.

event()->
	ok.

trans()->
	erlcat:init_cat("testapp",#cat_config{enable_heartbeat=0,enable_debugLog=1,encoder_type=1}),
    init_context(),
	send_trans(2),
	ok.

init_context()->
	Context = erlcat:new_context(),
    io:format("init_context ~p~n",[Context]),
    put(erlcat_process_context,Context).

% cat_test:init(),cat_test:trans().
send_trans(0)->
	ok;
send_trans(Index)->
    ErlCatContext = get(erlcat_process_context),
    io:format("contetxt ~p ~n",[ErlCatContext]),
	T1 = erlcat:new_transaction(ErlCatContext,"MSG.send", "send"),
	sleep1(),
	T2 = erlcat:new_transaction(ErlCatContext,"MSG.send", "check"),
	sleep1(),
	erlcat:complete(ErlCatContext,T2),
	T3 = erlcat:new_transaction(ErlCatContext,"MSG.send", "del111111"),
	sleep1(),
	erlcat:complete(ErlCatContext,T3),
	erlcat:complete(ErlCatContext,T1),
	io:format("send ~p~n",[Index]),
	send_trans(Index-1).


send_trans2(0)->
    ok;
send_trans2(Index)->
    ErlCatContext = get(erlcat_process_context),
    io:format("contetxt ~p ~n",[ErlCatContext]),
    T1 = erlcat:new_transaction(ErlCatContext,"MSG.send", "tttt1"),
    sleep1(),
    T2 = erlcat:new_transaction(ErlCatContext,"MSG.send", "tttt1_check"),
    sleep1(),
    erlcat:complete(ErlCatContext,T2),
    T3 = erlcat:new_transaction(ErlCatContext,"MSG.send", "tttt1_del"),
    sleep1(),
    erlcat:complete(ErlCatContext,T3),
    erlcat:complete(ErlCatContext,T1),
    io:format("send2 ~p~n",[Index]),
    send_trans2(Index-1).


sleep1()->

	timer:sleep(rand:uniform(200)).

heart_count(0)->
	ok;
heart_count(Count)->
	heart(),
	heart_count(Count-1).

heart()->
	% erlcat:init_cat("testapp",#cat_config{enable_heartbeat=1,enable_debugLog=1}),
    Data = #{
        "userinfo" => integer_to_list(rand:uniform(1000)),
        "test22" => integer_to_list(rand:uniform(1000)),
        "test333" => integer_to_list(rand:uniform(1000))
    },
    erlcat:log_heartbeat("titleh1",Data),
    ok.

trans_count(0)->
	ok;
trans_count(Count)->
	trans_with_due(),
	trans_count(Count-1).

trans_with_due()->
	erlcat:log_transaction_with_duration("TEST","testDuration",rand:uniform(200)).


mutil_trans()->
    erlcat:init_cat("testapp",#cat_config{enable_heartbeat=0,enable_debugLog=1,encoder_type=1}),
    spawn(fun()->
        init_context(),
        send_trans(100)
        end),
    spawn(fun()->
        init_context(),
        send_trans2(100)
          end).


l1()->
    erlcat:init_cat("testapp",#cat_config{enable_heartbeat=0,enable_debugLog=1,encoder_type=1}),
    Content = erlcat:new_context(),
    {R,P,C}= erlcat:log_remote_call_client(Content),
    io:format("get message tree id ~p ~p ~p ~n",[R,P,C]),
    erlcat:log_remote_call_server(Content,R,P,C),
    ok.
