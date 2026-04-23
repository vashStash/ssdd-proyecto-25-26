from flask_wtf import FlaskForm
from wtforms import (StringField, PasswordField, BooleanField, FileField)
from wtforms.validators import InputRequired, Length, Email

class LoginForm(FlaskForm):
    email = StringField('email', validators=[Email()])
    password = PasswordField('password', validators=[InputRequired()])
    remember_me = BooleanField('remember_me')

class RegisterForm(FlaskForm):
    id = StringField('Identificador único', validators=[InputRequired(message='Ese identificador ya está en uso')])
    email = StringField('E-mail', validators=[InputRequired(), Email(message='La dirección de correo no es válida')])
    username = StringField('Nombre de usuario', validators=[InputRequired()])
    password = PasswordField('Contraseña', validators=[InputRequired()])#EqualTo('confirm', message='Passwords must match')
    #confirm = PasswordField('Repeat Password')

class ProfileUpdateForm(FlaskForm):
    newname = StringField('newname', validators=[])
    newmail = StringField('newmail', validators=[Email(message='La dirección de correo no es válida')])

class PasswordUpdateForm(FlaskForm):
    oldPassword = PasswordField('oldpass', validators=[InputRequired()])#EqualTo('confirm', message='Passwords must match')
    newPassword = PasswordField('newpass', validators=[InputRequired()])

