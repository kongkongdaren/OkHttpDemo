package com.wen.asyl.okhttpdemo;

import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.http2.Http2Connection;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;


/**
 * Description：xx <br/>
 * Copyright (c) 2018<br/>
 * This program is protected by copyright laws <br/>
 * Date:2018-07-13 11:45
 *
 * @author 姜文莒
 * @version : 1.0
 */
public class CountingRequestBody extends RequestBody {
    protected  RequestBody delegate;
    private Listener listener;
    private CountingSink countingSink;

    public CountingRequestBody(RequestBody delegate, Listener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return null;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        countingSink=new CountingSink(sink);
        BufferedSink bufferedSink= Okio.buffer(countingSink);
        delegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }
    public  static interface Listener{
        void onRequestProgress(long byteWrited,long contentLength);
    }

    @Override
    public long contentLength() throws IOException {
        try {
            return delegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
            {
                return -1;
            }
        }
    }
    protected  final  class CountingSink extends ForwardingSink{
        private long bytesWritten;
        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWritten+=byteCount;
            listener.onRequestProgress(bytesWritten,contentLength());
        }
    }
}
