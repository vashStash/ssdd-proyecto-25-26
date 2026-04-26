from flask_login import UserMixin
import hashlib

users = []

class User(UserMixin):

    def __init__(self, id, name, email, password, is_admin=False, visits = 0):
        self.id = id
        self.name = name
        self.email = email
        self.password = hashlib.sha256(password.encode('utf-8')).hexdigest()
        self.is_admin = is_admin
        self.chatlist = []
        self.visits = visits

    def set_password(self, password):
        self.password = hashlib.sha256(password.encode('utf8')).hexdigest()

    def check_password(self, password):
        return self.password == hashlib.sha256(password.encode('utf8')).hexdigest()

    def get_user(email):
        for user in users:
            if user.email == email:
                return user
            return None
        
    def get_current_chatlist(user):
        return user.chatlist

    def __repr__(self):
        return '<User {}>'.format(self.email)
    
    def hashPassword(password):
        return hashlib.sha256(password.encode('utf8')).hexdigest()


class Chat():
    
    def __init__(self, id, name, nextToken, status, list):
        self.id = id
        self.name = name
        self.nextToken = nextToken
        self.status = status
        self.convers = list

    def __repr__(self):
        return '<Chat {}>'.format(self.name)


class Conversation():

    def __init__(self, id, prompt, answer, timestamp):
        self.id = id
        self.prompt = prompt
        self.answer = answer
        self.timestamp = timestamp

    def __repr__(self):
        return '<Conversation {}>'.format(self.prompt)
    