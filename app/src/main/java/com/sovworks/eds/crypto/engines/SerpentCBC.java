package com.sovworks.eds.crypto.engines;

import com.sovworks.eds.crypto.BlockCipherNative;
import com.sovworks.eds.crypto.CipherFactory;
import com.sovworks.eds.crypto.blockciphers.Serpent;
import com.sovworks.eds.crypto.modes.CBC;


public class SerpentCBC extends CBC {
    public SerpentCBC() {
        super(new CipherFactory() {

            @Override
            public int getNumberOfCiphers() {
                return 1;
            }

            @Override
            public BlockCipherNative createCipher(int typeIndex) {
                return new Serpent();
            }
        });
    }

    @Override
    public String getCipherName() {
        return "serpent";
    }


    @Override
    public int getKeySize() {
        return 32;
    }
}

    