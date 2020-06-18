-module(erlcat).

-include("erlcat.hrl").

-export([init/0, init_cat/2,init_cat/6, get_cat_version/0, is_cat_enabled/0, destroy_cat/0,new_context/0]).
-export([log_event/5, log_error/3, log_metric_for_count/3, log_metric_for_duration/3, log_metric_for_sum/3, log_transaction_with_duration/4]).
-export([new_transaction/3, set_status/3, set_timestamp/3, set_duration/3, set_duration_start/3, add_data/3, add_kv/4, complete/2]).
-export([create_message_id/1, create_remote_message_id/2, get_message_tree_id/1, get_message_tree_root_id/1, get_message_tree_parent_id/1]).
-export([set_message_tree_id/2, set_message_tree_root_id/2, set_message_tree_parent_id/2]).
-export([log_heartbeat/3]).
-export([log_remote_call_client/1,log_remote_call_server/4]).

-on_load(init/0).

-define(APPNAME, erlcat).
-define(LIBNAME, erlcat).
-define(NOT_LOADED, not_loaded(?LINE)).

init() ->
    SoName = case code:priv_dir(?APPNAME) of
        {error, bad_name} ->
            case filelib:is_dir(filename:join(["..", priv])) of
                true ->
                    filename:join(["..", priv, ?LIBNAME]);
                _ ->
                    filename:join([priv, ?LIBNAME])
            end;
        Dir ->
            filename:join(Dir, ?LIBNAME)
    end,
    erlang:load_nif(SoName, 0).

not_loaded(Line) ->
    erlang:nif_error({not_loaded, [{module, ?MODULE}, {line, Line}]}).

%% === Common Apis ===
init_cat(AppKey, CatConfig)->
    #cat_config {
        encoder_type = EncodeType,
        enable_heartbeat = HeartBeat,
        enable_sampling = Sampling,
        enable_multiprocessing = MultiProcess,
        enable_debugLog = Debug
    } = CatConfig,
    init_cat(AppKey, EncodeType, HeartBeat, Sampling, MultiProcess, Debug).

init_cat(_AppKey, _EncodeType, _HeartBeat, _Sampling, _MultiProcess, _Debug) ->
    ?NOT_LOADED.

get_cat_version() ->
    ?NOT_LOADED.

is_cat_enabled() ->
    ?NOT_LOADED.

destroy_cat() ->
    ?NOT_LOADED.

new_context()->
    ?NOT_LOADED.

%% === Event Apis ===

log_event(_ErlCatContext, _Type, _Name, _Status, _Data) ->
    ?NOT_LOADED.

log_error(_ErlCatContext, _Message, _ErrStr) ->
    ?NOT_LOADED.

%% === Metric Apis ===

log_metric_for_count(_ErlCatContext, _Name, _Count) ->
    ?NOT_LOADED.

log_metric_for_duration(_ErlCatContext, _Name, _Duration) ->
    ?NOT_LOADED.

log_metric_for_sum(_ErlCatContext, _Name, _Value) ->
    ?NOT_LOADED.

%% === Transaction Apis ===

log_transaction_with_duration(_ErlCatContext, _Type, _Name, _Duration) ->
    ?NOT_LOADED.

new_transaction(_ErlCatContext,_Name, _Type) ->
	?NOT_LOADED.

set_status(_ErlCatContext, _CatTransaction, _State)->
	?NOT_LOADED.

set_timestamp(_ErlCatContext, _CatTransaction, _Timestamp)->
	?NOT_LOADED.

set_duration(_ErlCatContext, _CatTransaction, _Duration)->
	?NOT_LOADED.

set_duration_start(_ErlCatContext, _CatTransaction, _DurationStart)->
	?NOT_LOADED.

add_data(_ErlCatContext, _CatTransaction, _Data)->
	?NOT_LOADED.

add_kv(_ErlCatContext, _CatTransaction, _Key, _Value)->
	?NOT_LOADED.

complete(_ErlCatContext,_CatTransaction)->
    ?NOT_LOADED.

%% === MessageId Apis ===    

create_message_id(_ErlCatContext) ->
    ?NOT_LOADED. 

create_remote_message_id(_ErlCatContext, _AppKey) ->
    ?NOT_LOADED. 

get_message_tree_id(_ErlCatContext) ->
    ?NOT_LOADED. 

get_message_tree_root_id(_ErlCatContext) ->
    ?NOT_LOADED. 

get_message_tree_parent_id(_ErlCatContext) ->
    ?NOT_LOADED. 

set_message_tree_id(_ErlCatContext, _MessageId) ->
    ?NOT_LOADED. 

set_message_tree_root_id(_ErlCatContext, _MessageId) ->
    ?NOT_LOADED. 

set_message_tree_parent_id(_ErlCatContext, _MessageId) ->
    ?NOT_LOADED.

log_heartbeat(_ErlCatContext, _HeartbeatCategory, _HeartMap)->
    ?NOT_LOADED.

log_remote_call_client(_ErlCatContext)->
    ?NOT_LOADED.
log_remote_call_server(_ErlCatContext, _RootId,_ParentId, _ChildId)->
    ?NOT_LOADED.