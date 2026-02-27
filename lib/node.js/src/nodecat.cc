#include <node.h>

#include "client.h"
#include "debug.h"

using namespace v8;
using namespace std;

namespace catapi {

    /**
     * Initialize cat
     * @param args
     */
    void Init(const FunctionCallbackInfo<Value> &args) {
        Isolate *isolate = args.GetIsolate();
        if (args.Length() < 1) {
            isolate->ThrowException(Exception::TypeError(String::NewFromUtf8(isolate, "Appkey is required")));
            return;
        }
        String::Utf8Value str(isolate, args[0]);

        CatClientConfig config = DEFAULT_CCAT_CONFIG;
        config.enableHeartbeat = 0;
        config.enableMultiprocessing = 1;
        catClientInitWithConfig((const char *) (*str), &config);
    }

    /**
     * Destroy cat
     * @param args
     */
    void Destroy(const FunctionCallbackInfo<Value> &args) {
        catClientDestroy();
    }

    void InnerSendNode(Isolate *isolate, Local<Object> node) {
        if (node->IsNull() || node->IsUndefined()) {
            return;
        }
        Local<Context> ctx = isolate->GetCurrentContext();
        String::Utf8Value messageType(isolate, node->Get(String::NewFromUtf8(isolate, "messageType")));
        if (strcmp(*messageType, "transaction") == 0) {
            String::Utf8Value type(isolate, node->Get(String::NewFromUtf8(isolate, "type")));
            String::Utf8Value name(isolate, node->Get(String::NewFromUtf8(isolate, "name")));
            String::Utf8Value status(isolate, node->Get(String::NewFromUtf8(isolate, "status")));
            String::Utf8Value data(isolate, node->Get(String::NewFromUtf8(isolate, "data")));
            double begin = (node->Get(String::NewFromUtf8(isolate, "beginTimestamp"))->NumberValue(ctx)).ToChecked();
            double end = (node->Get(String::NewFromUtf8(isolate, "endTimestamp"))->NumberValue(ctx)).ToChecked();

            CatTransaction *t = newTransaction((const char *) (*type), (const char *) (*name));
            t->setDurationInMillis(t, static_cast<unsigned long long int>(end - begin));
            t->setTimestamp(t, static_cast<unsigned long long int>(begin));
            t->setStatus(t, (const char *) (*status));
            t->addData(t, (const char *) (*data));

            // Iterate children recursively
            Array *children = Array::Cast((*(node->Get(String::NewFromUtf8(isolate, "children")))));
            int count = children->Length();
            for (u_int32_t i = 0; i < count; i++) {
                InnerSendNode(isolate, Object::Cast((*(children->Get(i))))->Clone());
            }

            t->complete(t);
        } else if (strcmp(*messageType, "event") == 0) {
            String::Utf8Value type(isolate, node->Get(String::NewFromUtf8(isolate, "type")));
            String::Utf8Value name(isolate, node->Get(String::NewFromUtf8(isolate, "name")));
            String::Utf8Value status(isolate, node->Get(String::NewFromUtf8(isolate, "status")));
            String::Utf8Value data(isolate, node->Get(String::NewFromUtf8(isolate, "data")));
            double begin(((node->Get(String::NewFromUtf8(isolate, "beginTimestamp")))->NumberValue(ctx)).ToChecked());

            CatEvent *e = newEvent((const char *) (*type), (const char *) (*name));
            e->setTimestamp(e, static_cast<unsigned long long int>(begin));
            e->addData(e, (const char *) (*data));
            e->setStatus(e, (const char *) (*status));
            e->complete(e);
        } else if (strcmp(*messageType, "heartbeat") == 0) {
            String::Utf8Value type(isolate, node->Get(String::NewFromUtf8(isolate, "type")));
            String::Utf8Value name(isolate, node->Get(String::NewFromUtf8(isolate, "name")));
            String::Utf8Value data(isolate, node->Get(String::NewFromUtf8(isolate, "data")));

            CatHeartBeat *h = newHeartBeat((const char *) (*type), (const char *) (*name));
            h->addData(h, (const char *) (*data));
            h->setStatus(h, CAT_SUCCESS);
            h->complete(h);
        }
    }

    void SendTree(const FunctionCallbackInfo<Value> &args) {
        Isolate *isolate = args.GetIsolate();
        if (args.Length() == 0) {
            return;
        }
        Array *tree = Array::Cast((*args[0]));

        Array *root = Array::Cast((*(tree->Get(String::NewFromUtf8(isolate, "root")))));

        InnerSendNode(isolate, root->Clone());
    }

    void exports(Local<Object> exports) {
        NODE_SET_METHOD(exports, "init", Init);
        NODE_SET_METHOD(exports, "destroy", Destroy);
        NODE_SET_METHOD(exports, "sendTree", SendTree);
    }

    NODE_MODULE(nodecat, exports)

} // namespace catapi
