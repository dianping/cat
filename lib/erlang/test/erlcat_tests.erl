%%%-------------------------------------------------------------------
%%% @author dlive
%%% @copyright (C) 2019, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 06. Apr 2019 3:25 PM
%%%-------------------------------------------------------------------
-module(erlcat_tests).
-author("dlive").

-include_lib("eunit/include/eunit.hrl").
-include("erlcat.hrl").
-compile(export_all).


setup() ->
    erlcat:init_cat("testapp", #cat_config{enable_heartbeat = 0, enable_debugLog = 1, encoder_type = 1}),
    ok.


remote_call_client_test()->
    setup(),
    Content = erlcat:new_context(),
    erlcat:log_remote_call_client(Content),
    ok.

transaction_test_() ->
    {
        setup,
        spawn,
        fun() -> setup() end,
        [
            fun() ->
                init_context(),
                send_trans(4) end,
            fun() ->
                send_trans2(4) end
        ]
    }.


logevent_test_() ->
    {
        setup,
        spawn,
        fun() -> setup() end,
        [
            fun() ->
                init_context(),
                erlcat:log_event(get(erlcat_process_context), "EVENT_TYPE", "EVENT_NAME", "0", "data")
            end,
            fun() ->
                erlcat:log_event(get(erlcat_process_context), "EVENT_TYPE", "EVENT_NAME", "0", "data")
            end
        ]
    }.

logerror_test_() ->
    {
        setup,
        spawn,
        fun() -> setup() end,
        [
            fun() ->
                init_context(),
                erlcat:log_error(get(erlcat_process_context), "errormessage", "detail info")
            end,
            fun() ->
                erlcat:log_error(get(erlcat_process_context), "errormessage","detail info")
            end
        ]
    }.

heart_test_() ->
    {
        setup,
        spawn,
        fun() -> setup() end,
        [
            fun() ->
                init_context(),
                heart_count(4) end,
            fun() ->
                heart_count(4) end
        ]
    }.

init_context() ->
    Context = erlcat:new_context(),
    put(erlcat_process_context, Context).

% cat_test:init(),cat_test:trans().
send_trans(0) ->
    ok;
send_trans(Index) ->
    ErlCatContext = get(erlcat_process_context),
    io:format(user, "contetxt111 ~p ~n", [ErlCatContext]),
    T1 = erlcat:new_transaction(ErlCatContext, "MSG.send", "send"),
    sleep1(),
    T2 = erlcat:new_transaction(ErlCatContext, "MSG.send", "check"),
    sleep1(),
    erlcat:complete(ErlCatContext, T2),
    T3 = erlcat:new_transaction(ErlCatContext, "MSG.send", "del111111"),
    sleep1(),
    erlcat:complete(ErlCatContext, T3),
    erlcat:complete(ErlCatContext, T1),
    send_trans(Index - 1).



send_trans2(0) ->
    ok;
send_trans2(Index) ->
    ErlCatContext = get(erlcat_process_context),
    io:format(user, "contetxt ~p ~n", [ErlCatContext]),
    T1 = erlcat:new_transaction(ErlCatContext, "MSG.send", "tttt1"),
    sleep1(),
    T2 = erlcat:new_transaction(ErlCatContext, "MSG.send", "tttt1_check"),
    sleep1(),
    erlcat:complete(ErlCatContext, T2),
    T3 = erlcat:new_transaction(ErlCatContext, "MSG.send", "tttt1_del"),
    sleep1(),
    erlcat:complete(ErlCatContext, T3),
    erlcat:complete(ErlCatContext, T1),
    io:format(user, "send2 ~p~n", [Index]),
    send_trans2(Index - 1).


sleep1() ->

    timer:sleep(rand:uniform(200)).




heart_count(0) ->
    ok;
heart_count(Count) ->
    heart(),
    heart_count(Count - 1).

heart() ->
    % erlcat:init_cat("testapp",#cat_config{enable_heartbeat=1,enable_debugLog=1}),
    Data = #{
        "userinfo" => integer_to_list(rand:uniform(1000)),
        "test22" => integer_to_list(rand:uniform(1000)),
        "test333" => integer_to_list(rand:uniform(1000))
    },
    erlcat:log_heartbeat(get(erlcat_process_context), "titleh1", Data),
    ok.