# -*- coding: utf-8 -*-
import os
import json
import random
import binascii
import sys
import struct
import base64

import requests
from urlobject import URLObject
from Crypto.Cipher import AES
from Crypto.PublicKey import RSA
from Crypto.Util import Counter

#copied from utils
def a32_to_str(a):
	return struct.pack('>%dI' % len(a), *a)


def aes_cbc_encrypt(data, key):
	encryptor = AES.new(key, AES.MODE_CBC, '\0' * 16)
	return encryptor.encrypt(data)


def aes_cbc_encrypt_a32(data, key):
	return str_to_a32(aes_cbc_encrypt(a32_to_str(data), a32_to_str(key)))


def str_to_a32(b):
	if len(b) % 4:  # Add padding, we need a string with a length multiple of 4
		b += '\0' * (4 - len(b) % 4)
	return struct.unpack('>%dI' % (len(b) / 4), b)


def mpi2int(s):
	return int(binascii.hexlify(s[2:]), 16)


def aes_cbc_decrypt(data, key):
	decryptor = AES.new(key, AES.MODE_CBC, '\0' * 16)
	return decryptor.decrypt(data)


def aes_cbc_decrypt_a32(data, key):
	return str_to_a32(aes_cbc_decrypt(a32_to_str(data), a32_to_str(key)))


def base64urldecode(data):
	data += '=='[(2 - len(data) * 3) % 4:]
	for search, replace in (('-', '+'), ('_', '/'), (',', '')):
		data = data.replace(search, replace)
	return base64.b64decode(data)


def base64_to_a32(s):
	return str_to_a32(base64urldecode(s))


def base64urlencode(data):
	data = base64.b64encode(data)
	for search, replace in (('+', '-'), ('/', '_'), ('=', '')):
		data = data.replace(search, replace)
	return data

def a32_to_base64(a):
	return base64urlencode(a32_to_str(a))


def get_chunks(size):
	chunks = {}
	p = pp = 0
	i = 1

	while i <= 8 and p < size - i * 0x20000:
		chunks[p] = i * 0x20000
		pp = p
		p += chunks[p]
		i += 1

	while p < size:
		chunks[p] = 0x100000
		pp = p
		p += chunks[p]

	chunks[pp] = size - pp
	if not chunks[pp]:
		del chunks[pp]

	return chunks

# copied from crypto
def aes_cbc_encrypt(data, key):
	encryptor = AES.new(key, AES.MODE_CBC, '\0' * 16)
	return encryptor.encrypt(data)


def aes_cbc_decrypt(data, key):
	decryptor = AES.new(key, AES.MODE_CBC, '\0' * 16)
	return decryptor.decrypt(data)


def aes_cbc_encrypt_a32(data, key):
	return str_to_a32(aes_cbc_encrypt(a32_to_str(data), a32_to_str(key)))


def aes_cbc_decrypt_a32(data, key):
	return str_to_a32(aes_cbc_decrypt(a32_to_str(data), a32_to_str(key)))


def stringhash(s, aeskey):
	s32 = str_to_a32(s)
	h32 = [0, 0, 0, 0]
	for i in xrange(len(s32)):
		h32[i % 4] ^= s32[i]
	for _ in xrange(0x4000):
		h32 = aes_cbc_encrypt_a32(h32, aeskey)
	return a32_to_base64((h32[0], h32[2]))


def prepare_key(a):
	pkey = [0x93C467E3, 0x7DB0C7A4, 0xD1BE3F81, 0x0152CB56]
	for _ in xrange(0x10000):
		for j in xrange(0, len(a), 4):
			key = [0, 0, 0, 0]
			for i in xrange(4):
				if i + j < len(a):
					key[i] = a[i + j]
			pkey = aes_cbc_encrypt_a32(pkey, key)
	return pkey


def encrypt_key(a, key):
	return sum(
		(aes_cbc_encrypt_a32(a[i:i+4], key)
			for i in xrange(0, len(a), 4)), ())


def decrypt_key(a, key):
	return sum(
		(aes_cbc_decrypt_a32(a[i:i+4], key)
			for i in xrange(0, len(a), 4)), ())


def enc_attr(attr, key):
	attr = 'MEGA' + json.dumps(attr)
	if len(attr) % 16:
		attr += '\0' * (16 - len(attr) % 16)
	return aes_cbc_encrypt(attr, a32_to_str(key))


def dec_attr(attr, key):
	attr = aes_cbc_decrypt(attr, a32_to_str(key)).rstrip('\0')
	return json.loads(attr[4:])


class Mega(object):
	def __init__(self):
		self.seqno = random.randint(0, 0xFFFFFFFF)
		self.sid = None

	@classmethod
	def from_credentials(cls, email, password):
		inst = cls()
		inst.login_user(email, password)
		return inst

	@classmethod
	def from_ephemeral(cls):
		inst = cls()
		inst.login_ephemeral()
		return inst

	def api_req(self, data):
		params = {'id': self.seqno}
		self.seqno += 1
		if self.sid:
			params.update({'sid': self.sid})
		data = json.dumps([data])
		req = requests.post(
			'https://g.api.mega.co.nz/cs', params=params, data=data)
		json_data = req.json()
		if isinstance(json_data, int):
			raise MegaRequestException(json_data)
		return json_data[0]

	def login_user(self, email, password):
		password_aes = prepare_key(str_to_a32(password))
		uh = stringhash(email, password_aes)
		res = self.api_req({'a': 'us', 'user': email, 'uh': uh})
		self._login_common(res, password_aes)

	def login_ephemeral(self):
		random_master_key = [random.randint(0, 0xFFFFFFFF)] * 4
		random_password_key = [random.randint(0, 0xFFFFFFFF)] * 4
		random_session_self_challenge = [random.randint(0, 0xFFFFFFFF)] * 4
		user_handle = self.api_req({
			'a': 'up',
			'k': a32_to_base64(encrypt_key(random_master_key,
										   random_password_key)),
			'ts': base64urlencode(a32_to_str(random_session_self_challenge) +
								  a32_to_str(encrypt_key(
									  random_session_self_challenge,
									  random_master_key)))
		})
		res = self.api_req({'a': 'us', 'user': user_handle})
		self._login_common(res, random_password_key)

	def _login_common(self, res, password):
		enc_master_key = base64_to_a32(res['k'])
		self.master_key = decrypt_key(enc_master_key, password)
		if 'tsid' in res:
			tsid = base64urldecode(res['tsid'])
			key_encrypted = a32_to_str(
				encrypt_key(str_to_a32(tsid[:16]), self.master_key))
			if key_encrypted == tsid[-16:]:
				self.sid = res['tsid']
		elif 'csid' in res:
			enc_rsa_priv_key = base64_to_a32(res['privk'])
			rsa_priv_key = decrypt_key(enc_rsa_priv_key, self.master_key)

			privk = a32_to_str(rsa_priv_key)
			self.rsa_priv_key = [0, 0, 0, 0]

			for i in xrange(4):
				l = ((ord(privk[0]) * 256 + ord(privk[1]) + 7) / 8) + 2
				self.rsa_priv_key[i] = mpi2int(privk[:l])
				privk = privk[l:]

			enc_sid = mpi2int(base64urldecode(res['csid']))
			decrypter = RSA.construct(
				(self.rsa_priv_key[0] * self.rsa_priv_key[1],
				 0L,
				 self.rsa_priv_key[2],
				 self.rsa_priv_key[0],
				 self.rsa_priv_key[1]))
			sid = '%x' % decrypter.key._decrypt(enc_sid)
			sid = binascii.unhexlify('0' + sid if len(sid) % 2 else sid)
			self.sid = base64urlencode(sid[:43])

	def get_files(self):
		files_data = self.api_req({'a': 'f', 'c': 1})
		for file in files_data['f']:
			if file['t'] in (0, 1):
				key = file['k'].split(':')[1]
				key = decrypt_key(base64_to_a32(key), self.master_key)
				# file
				if file['t'] == 0:
					k = (key[0] ^ key[4],
						 key[1] ^ key[5],
						 key[2] ^ key[6],
						 key[3] ^ key[7])
				# directory
				else:
					k = key
				attributes = base64urldecode(file['a'])
				attributes = dec_attr(attributes, k)
				file['a'] = attributes
				file['k'] = key
			# Root ("Cloud Drive")
			elif file['t'] == 2:
				self.root_id = file['h']
			# Inbox
			elif file['t'] == 3:
				self.inbox_id = file['h']
			# Trash Bin
			elif file['t'] == 4:
				self.trashbin_id = file['h']
		return files_data

	def download_from_url(self, url,file_name):
		url_object = URLObject(url)
		file_id, file_key = url_object.fragment[1:].split('!')
		self.download_file(file_id, file_key, file_name, public=True)

	def download_file(self, file_id, file_key, file_name=None, public=False):
		if public:
			file_key = base64_to_a32(file_key)
			file_data = self.api_req({'a': 'g', 'g': 1, 'p': file_id})
		else:
			file_data = self.api_req({'a': 'g', 'g': 1, 'n': file_id})

		k = (file_key[0] ^ file_key[4],
			 file_key[1] ^ file_key[5],
			 file_key[2] ^ file_key[6],
			 file_key[3] ^ file_key[7])
		iv = file_key[4:6] + (0, 0)
		meta_mac = file_key[6:8]

		file_url = file_data['g']
		file_size = file_data['s']
		attributes = base64urldecode(file_data['at'])
		attributes = dec_attr(attributes, k)

		infile = requests.get(file_url, stream=True).raw
		if file_name == None:
			if sys.platform == 'win32':
					import msvcrt
					msvcrt.setmode(sys.stdout.fileno(), os.O_BINARY)
			outfile = sys.stdout 
		else:
		  outfile = open(file_name, 'wb')     

		counter = Counter.new(
			128, initial_value=((iv[0] << 32) + iv[1]) << 64)
		decryptor = AES.new(a32_to_str(k), AES.MODE_CTR, counter=counter)

		file_mac = (0, 0, 0, 0)
		for chunk_start, chunk_size in sorted(get_chunks(file_size).items()):
			chunk = infile.read(chunk_size)
			chunk = decryptor.decrypt(chunk)
			outfile.write(chunk)

		outfile.close()

	def get_public_url(self, file_id, file_key):
		public_handle = self.api_req({'a': 'l', 'n': file_id})
		decrypted_key = a32_to_base64(file_key)
		return 'http://mega.co.nz/#!%s!%s' % (public_handle, decrypted_key)

	def uploadfile(self, filename, dst=None):
		if not dst:
			root_id = getattr(self, 'root_id')
			if not root_id:
				self.get_files()
			dst = self.root_id
		infile = open(filename, 'rb')
		size = os.path.getsize(filename)
		ul_url = self.api_req({'a': 'u', 's': size})['p']

		ul_key = [random.randint(0, 0xFFFFFFFF) for _ in xrange(6)]
		counter = Counter.new(
			128, initial_value=((ul_key[4] << 32) + ul_key[5]) << 64)
		encryptor = AES.new(
			a32_to_str(ul_key[:4]),
			AES.MODE_CTR,
			counter=counter)

		file_mac = [0, 0, 0, 0]
		for chunk_start, chunk_size in sorted(get_chunks(size).items()):
			chunk = infile.read(chunk_size)

			chunk_mac = [ul_key[4], ul_key[5], ul_key[4], ul_key[5]]
			for i in xrange(0, len(chunk), 16):
				block = chunk[i:i+16]
				if len(block) % 16:
					block += '\0' * (16 - len(block) % 16)
				block = str_to_a32(block)
				chunk_mac = [chunk_mac[0] ^ block[0],
							 chunk_mac[1] ^ block[1],
							 chunk_mac[2] ^ block[2],
							 chunk_mac[3] ^ block[3]]
				chunk_mac = aes_cbc_encrypt_a32(chunk_mac, ul_key[:4])

			file_mac = [file_mac[0] ^ chunk_mac[0],
						file_mac[1] ^ chunk_mac[1],
						file_mac[2] ^ chunk_mac[2],
						file_mac[3] ^ chunk_mac[3]]
			file_mac = aes_cbc_encrypt_a32(file_mac, ul_key[:4])

			chunk = encryptor.encrypt(chunk)
			url = '%s/%s' % (ul_url, str(chunk_start))
			outfile = requests.post(url, data=chunk).raw
			completion_handle = outfile.read()
		infile.close()

		meta_mac = (file_mac[0] ^ file_mac[1], file_mac[2] ^ file_mac[3])

		attributes = {'n': os.path.basename(filename)}
		enc_attributes = base64urlencode(enc_attr(attributes, ul_key[:4]))
		key = [ul_key[0] ^ ul_key[4],
			   ul_key[1] ^ ul_key[5],
			   ul_key[2] ^ meta_mac[0],
			   ul_key[3] ^ meta_mac[1],
			   ul_key[4], ul_key[5],
			   meta_mac[0], meta_mac[1]]
		encrypted_key = a32_to_base64(encrypt_key(key, self.master_key))
		data = self.api_req({'a': 'p', 't': dst, 'n': [
			{'h': completion_handle,
			 't': 0,
			 'a': enc_attributes,
			 'k': encrypted_key}]})
		return data

def main(argv):
	m = Mega.from_ephemeral()
	if len(sys.argv) > 2:
		m.download_from_url(sys.argv[1],sys.argv[2])
	else:
		m.download_from_url(sys.argv[1],None)
		
if __name__ == "__main__":
	main(sys.argv[1:])