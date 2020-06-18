%%%-------------------------------------------------------------------
%%% @author dlive
%%% @copyright (C) 2019, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 25. Feb 2019 4:33 PM
%%%-------------------------------------------------------------------
-author("dlive").

-define(ENCODE_TEXT,0).
-define(ENCODE_BINARY,1).

-record(cat_config, {encoder_type=1, enable_heartbeat=1, enable_sampling=1, enable_multiprocessing=0, enable_debugLog=0}).