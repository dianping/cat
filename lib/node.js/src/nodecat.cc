#include <node.h>

#include "client.h"
#include "debug.h"

using namespace v8;
using namespace std;
#define MAJOR_VERSION NODE_MAJOR_VERSION
#define MINOR_VERSION NODE_MINOR_VERSION

namespace catapi
{

  /**
   * Initialize cat
   * @param args
   */
  void Init(const FunctionCallbackInfo<Value> &args)
  {
    Isolate *isolate = args.GetIsolate();
    if (args.Length() < 1)
    {
#if (MAJOR_VERSION <= 12)
      isolate->ThrowException(Exception::TypeError(String::NewFromUtf8(isolate, "Appkey is required")));
#else
      isolate->ThrowException(Exception::TypeError(String::NewFromUtf8(isolate, "Appkey is required").ToLocalChecked()));
#endif
      return;
    }
    String::Utf8Value str(isolate, args[0]);

    CatClientConfig config = DEFAULT_CCAT_CONFIG;
    config.enableHeartbeat = 0;
    config.enableMultiprocessing = 1;
    catClientInitWithConfig((const char *)(*str), &config);
  }

  /**
   * Destroy cat
   * @param args
   */
  void Destroy(const FunctionCallbackInfo<Value> &args)
  {
    catClientDestroy();
  }

  void InnerSendNode(Isolate *isolate, Local<Object> node)
  {
    if (node->IsNull() || node->IsUndefined())
    {
      return;
    }
    Local<Context> ctx = isolate->GetCurrentContext();
    cout << NODE_MODULE_VERSION << endl;
    std::cout << "Value is: " << NODE_MODULE_VERSION << std::endl;

#if (MAJOR_VERSION <= 12)
    String::Utf8Value messageType(isolate, node->Get(String::NewFromUtf8(isolate, "messageType")));
#else
    v8::Local<v8::String> messageTypeKey = v8::String::NewFromUtf8(isolate, "messageType").ToLocalChecked();
    v8::Local<v8::Value> messageTypeValue = node->Get(ctx, messageTypeKey).ToLocalChecked();
    v8::String::Utf8Value messageType(isolate, messageTypeValue);
#endif
    if (strcmp(*messageType, "transaction") == 0)
    {
#if (MAJOR_VERSION <= 12)
      String::Utf8Value type(isolate, node->Get(String::NewFromUtf8(isolate, "type")));
#else
      v8::Local<v8::String> typeKey = v8::String::NewFromUtf8(isolate, "type").ToLocalChecked();
      v8::Local<v8::Value> typeValue = node->Get(ctx, typeKey).ToLocalChecked();
      v8::String::Utf8Value type(isolate, typeValue);
#endif

#if (MAJOR_VERSION <= 12)
      String::Utf8Value name(isolate, node->Get(String::NewFromUtf8(isolate, "name")));
#else
      v8::Local<v8::String> nameKey = v8::String::NewFromUtf8(isolate, "name").ToLocalChecked();
      v8::Local<v8::Value> nameValue = node->Get(ctx, nameKey).ToLocalChecked();
      v8::String::Utf8Value name(isolate, nameValue);
#endif

#if (MAJOR_VERSION <= 12)
      String::Utf8Value status(isolate, node->Get(String::NewFromUtf8(isolate, "status")));
#else
      v8::Local<v8::String> statusKey = v8::String::NewFromUtf8(isolate, "status").ToLocalChecked();
      v8::Local<v8::Value> statusValue = node->Get(ctx, statusKey).ToLocalChecked();
      v8::String::Utf8Value status(isolate, statusValue);
#endif

#if (MAJOR_VERSION <= 12)
      String::Utf8Value data(isolate, node->Get(String::NewFromUtf8(isolate, "data")));
#else
      v8::Local<v8::String> dataKey = v8::String::NewFromUtf8(isolate, "data").ToLocalChecked();
      v8::Local<v8::Value> dataValue = node->Get(ctx, dataKey).ToLocalChecked();
      v8::String::Utf8Value data(isolate, dataValue);
#endif

#if (MAJOR_VERSION <= 12)
      double begin = (node->Get(String::NewFromUtf8(isolate, "beginTimestamp"))->NumberValue(ctx)).ToChecked();
#else
      v8::Local<v8::String> beginKey = v8::String::NewFromUtf8(isolate, "beginTimestamp").ToLocalChecked();
      v8::Local<v8::Value> beginValue = node->Get(ctx, beginKey).ToLocalChecked();
      double begin = beginValue->NumberValue(ctx).ToChecked();
#endif

#if (MAJOR_VERSION <= 12)
      double end = (node->Get(String::NewFromUtf8(isolate, "endTimestamp"))->NumberValue(ctx)).ToChecked();
#else
      v8::Local<v8::String> endKey = v8::String::NewFromUtf8(isolate, "endTimestamp").ToLocalChecked();
      v8::Local<v8::Value> endValue = node->Get(ctx, endKey).ToLocalChecked();
      double end = endValue->NumberValue(ctx).ToChecked();
#endif

      CatTransaction *t = newTransaction((const char *)(*type), (const char *)(*name));
      t->setDurationInMillis(t, static_cast<unsigned long long int>(end - begin));
      t->setTimestamp(t, static_cast<unsigned long long int>(begin));
      t->setStatus(t, (const char *)(*status));
      t->addData(t, (const char *)(*data));

// Iterate children recursively
#if (MAJOR_VERSION <= 12)
      Array *children = Array::Cast((*(node->Get(String::NewFromUtf8(isolate, "children")))));
#else
      v8::Local<v8::String> childrenKey = v8::String::NewFromUtf8(isolate, "children").ToLocalChecked();
      v8::Local<v8::Value> childrenValue = node->Get(ctx, childrenKey).ToLocalChecked();
      v8::Local<v8::Array> children = v8::Local<v8::Array>::Cast(childrenValue);
#endif

      int count = children->Length();
      for (int i = 0; i < count; i++)
      {
#if (MAJOR_VERSION <= 12)
        InnerSendNode(isolate, Object::Cast((*(children->Get(i))))->Clone());
#else
        v8::Local<v8::Value> childValue = children->Get(ctx, i).ToLocalChecked();
        v8::Local<v8::Object> childObject = v8::Local<v8::Object>::Cast(childValue);
        v8::Local<v8::Object> clonedChildObject = childObject->Clone();
        InnerSendNode(isolate, clonedChildObject);
#endif
      }

      t->complete(t);
    }
    else if (strcmp(*messageType, "event") == 0)
    {
#if (MAJOR_VERSION <= 12)
      String::Utf8Value type(isolate, node->Get(String::NewFromUtf8(isolate, "type")));
#else
      v8::Local<v8::String> typeKey = v8::String::NewFromUtf8(isolate, "type").ToLocalChecked();
      v8::Local<v8::Value> typeValue = node->Get(ctx, typeKey).ToLocalChecked();
      v8::String::Utf8Value type(isolate, typeValue);
#endif

#if (MAJOR_VERSION <= 12)
      String::Utf8Value name(isolate, node->Get(String::NewFromUtf8(isolate, "name")));
#else
      v8::Local<v8::String> nameKey = v8::String::NewFromUtf8(isolate, "name").ToLocalChecked();
      v8::Local<v8::Value> nameValue = node->Get(ctx, nameKey).ToLocalChecked();
      v8::String::Utf8Value name(isolate, nameValue);
#endif

#if (MAJOR_VERSION <= 12)
      String::Utf8Value status(isolate, node->Get(String::NewFromUtf8(isolate, "status")));
#else
      v8::Local<v8::String> statusKey = v8::String::NewFromUtf8(isolate, "status").ToLocalChecked();
      v8::Local<v8::Value> statusValue = node->Get(ctx, statusKey).ToLocalChecked();
      v8::String::Utf8Value status(isolate, statusValue);
#endif

#if (MAJOR_VERSION <= 12)
      String::Utf8Value data(isolate, node->Get(String::NewFromUtf8(isolate, "data")));
#else
      v8::Local<v8::String> dataKey = v8::String::NewFromUtf8(isolate, "data").ToLocalChecked();
      v8::Local<v8::Value> dataValue = node->Get(ctx, dataKey).ToLocalChecked();
      v8::String::Utf8Value data(isolate, dataValue);
#endif

#if (MAJOR_VERSION <= 12)
      double begin(((node->Get(String::NewFromUtf8(isolate, "beginTimestamp")))->NumberValue(ctx)).ToChecked());
#else
      v8::Local<v8::String> beginKey = v8::String::NewFromUtf8(isolate, "beginTimestamp").ToLocalChecked();
      v8::Local<v8::Value> beginValue = node->Get(ctx, beginKey).ToLocalChecked();
      double begin = beginValue->NumberValue(ctx).ToChecked();
#endif

      CatEvent *e = newEvent((const char *)(*type), (const char *)(*name));
      e->setTimestamp(e, static_cast<unsigned long long int>(begin));
      e->addData(e, (const char *)(*data));
      e->setStatus(e, (const char *)(*status));
      e->complete(e);
    }
    else if (strcmp(*messageType, "heartbeat") == 0)
    {
#if (MAJOR_VERSION <= 12)
      String::Utf8Value type(isolate, node->Get(String::NewFromUtf8(isolate, "type")));
#else
      v8::Local<v8::String> typeKey = v8::String::NewFromUtf8(isolate, "type").ToLocalChecked();
      v8::Local<v8::Value> typeValue = node->Get(ctx, typeKey).ToLocalChecked();
      v8::String::Utf8Value type(isolate, typeValue);
#endif

#if (MAJOR_VERSION <= 12)
      String::Utf8Value name(isolate, node->Get(String::NewFromUtf8(isolate, "name")));
#else
      v8::Local<v8::String> nameKey = v8::String::NewFromUtf8(isolate, "name").ToLocalChecked();
      v8::Local<v8::Value> nameValue = node->Get(ctx, nameKey).ToLocalChecked();
      v8::String::Utf8Value name(isolate, nameValue);
#endif

#if (MAJOR_VERSION <= 12)
      String::Utf8Value data(isolate, node->Get(String::NewFromUtf8(isolate, "data")));
#else
      v8::Local<v8::String> dataKey = v8::String::NewFromUtf8(isolate, "data").ToLocalChecked();
      v8::Local<v8::Value> dataValue = node->Get(ctx, dataKey).ToLocalChecked();
      v8::String::Utf8Value data(isolate, dataValue);
#endif

      CatHeartBeat *h = newHeartBeat((const char *)(*type), (const char *)(*name));
      h->addData(h, (const char *)(*data));
      h->setStatus(h, CAT_SUCCESS);
      h->complete(h);
    }
  }

  void SendTree(const FunctionCallbackInfo<Value> &args)
  {
    Isolate *isolate = args.GetIsolate();
    if (args.Length() == 0)
    {
      return;
    }
    Array *tree = Array::Cast((*args[0]));

#if (MAJOR_VERSION <= 12)
    Array *root = Array::Cast((*(tree->Get(String::NewFromUtf8(isolate, "root")))));
#else
    Local<Context> ctx = isolate->GetCurrentContext();
    v8::Local<v8::String> rootKey = v8::String::NewFromUtf8(isolate, "root").ToLocalChecked();
    v8::Local<v8::Value> rootValue = tree->Get(ctx, rootKey).ToLocalChecked();
    v8::Local<v8::Array> root = v8::Local<v8::Array>::Cast(rootValue);
#endif

    InnerSendNode(isolate, root->Clone());
  }

  void exports(Local<Object> exports)
  {
    NODE_SET_METHOD(exports, "init", Init);
    NODE_SET_METHOD(exports, "destroy", Destroy);
    NODE_SET_METHOD(exports, "sendTree", SendTree);
  }

  NODE_MODULE(nodecat, exports)

} // namespace catapi
