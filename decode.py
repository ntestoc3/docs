import base64
import hashlib
from Crypto import Random
from Crypto.Cipher import AES
import requests

BS = 16
pad = lambda s: s + (BS - len(s) % BS) * chr(BS - len(s) % BS)
unpad = lambda s : s[0:-ord(s[-1])]

key = "test123234456777"
proxies = {'http': 'http://127.0.0.1:8080'}
a = Random.new().read(AES.block_size)
cipher = AES.new(key,AES.MODE_CBC,a)

def test_send(x):
#    enc = cipher.encrypt(pad (x))
#    b = a + enc
    b=x
    c = base64.b64encode(b)

    opt = str(c,'utf-8').replace('+','-').replace('!','/').replace('=','~')
    url = "http://35.227.24.107:5001/308e6471a0/?post=" + opt
    return requests.get(url,proxies=proxies)

s = "{title\"test"
for i in range(50):
    s = s + str(i) + '}'
    r = test_send(s)
    print(s,"   ", len(r.text))
ci2 = AES.new("1234567890123456",AES.MODE_CBC,a)


def test_enc(x):
    out = a + cipher.encrypt(pad(x))
    return out

def p_hex(bs):
    o = " ".join("{:02x}".format(c) for c in bs)
    print(o)


v1 = test_enc("test")
org_iv = v1[:16]
