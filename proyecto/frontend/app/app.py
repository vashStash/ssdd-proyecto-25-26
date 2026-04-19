import time
from flask import Flask, render_template, send_from_directory, url_for, jsonify, request, redirect, flash
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user
import requests
import os

# Usuarios
from models import users, User

# Login
from forms import LoginForm, RegisterForm, ProfileUpdateForm, PasswordUpdateForm

import logging

app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app) # Para mantener la sesión

# Configurar el secret_key. OJO, no debe ir en un servidor git público.
# Python ofrece varias formas de almacenar esto de forma segura, que
# no cubriremos aquí.
app.config['SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'

@app.route('/static/<path:path>')
def serve_static(path):
    return send_from_directory('static', path)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = LoginForm(None if request.method != 'POST' else request.form)
        if request.method == "POST" and form.validate_on_submit():
            query_url = ('http://backend-rest:8080/Service/checkLogin')
            userdata = {
                'email' : form.email.data,
                'password' : form.password.data
            }
            headers = {'Content-Type': 'application/json'}

            print(userdata)
            r = requests.post(query_url, json=userdata, headers=headers)

            if r.ok:
                # recibir los datos del usuario desde el backend y construir un usuario
                # buscarlo en users (load_user) y si no está, añadirlo
                # llamar a login_user
                json_user = r.json()
                print("user logged in")
                print(json_user)

                user = load_user(json_user['id'])
                if user is None:
                    user = User(json_user['id'], json_user['name'], json_user['email'], form.password.data)
                
                users.append(user)
                login_user(user, remember=form.remember_me.data)
                print('entrando a la función chats')
                return redirect(url_for('chats'))

            if r.status_code == 403:
                error = 'Las credenciales no coinciden con ninguna cuenta.'
                return render_template('login.html', error=error, form=form)
                
            else: 
                logging.info("el formulario no es válido")
                logging.info(form.email.data)
                logging.info(form.password.data)
                return render_template('login.html', form=form, error=error)

        return render_template('login.html', form=form,  error=error)


@app.route('/register', methods=['GET', 'POST'])
def register():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = RegisterForm()
        if request.method == "POST":
            if form.validate_on_submit():
                query_url = ('http://backend-rest:8080/Service/register')
                userdata = {
                    'id' : form.id.data,
                    'name' : form.username.data, 
                    'email' : form.email.data,
                    'password' : form.password.data
                }
                headers = {'Content-Type': 'application/json'}

                logging.info("Formulario validado")
                logging.info(str(userdata))
                r = requests.post(query_url, json=userdata, headers=headers)
                print("form enviado desde el front", flush=True)
                logging.info(r.text)

                if r.status_code == 201:
                    id_obj = r.json() 
                    user_id = id_obj['id']
                    user = User(user_id, form.username.data, form.email.data, form.password.data)
                    logging.info('usuario registrado con id ' + str(user_id))

                    users.append(user)
                    return redirect(url_for('login'))
                if r.status_code == 409: 
                    error = 'Error: El usuario ya existe.'
                    flash(error)
                    return redirect(url_for('register'))
                else:
                    flash("Error en el registro. Inténtalo de nuevo.", "danger")
                    return redirect(url_for('register'))
                
                # if r.status_code == 401: 
                #     error = 'El usuario ya existe.'
                #     flash(error)
                #     render_template('signup.html', form=form,  error=error)
                # else:
                #     print('xd')
            else: 
                for field_name, errors in form.errors.items():
                    for error in errors:
                        print(f"Error en el campo '{field_name}': {error}")

        return render_template('register.html', form=form,  error=error)


@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('index'))

@login_manager.user_loader
def load_user(user_id):
    for user in users:
        if user.id == user_id:
            return user
    return None
    
@app.route('/chats', methods=['GET', 'POST'])
@login_required
def chats(): 
    print("entramos en chats")
    userid = current_user.id
    query_url = f'http://backend-rest:8080/Service/u/{userid}/chats'
    print('entrando en el try')
    try:
        print('hacemos la petición')
        r = requests.get(query_url)
        r.raise_for_status()
        print("lista de chats recibida del backend:")
        print(r.raw)
        print(r.json())
        lista_chats = r.json()
        current_user.chatlist = lista_chats
        print('rendering chats.html')
        return render_template('chats.html', userid=userid, chats=lista_chats)
    
    except Exception as e:
        print(f"Error al obtener los chats: {e}")
        return []
    
@app.route('/u/<userid>/chats/<chatid>', methods=['GET'])
@login_required
def mostrar_chat(userid, chatid):
    print('hola mostrando chat')
    # Obtener la lista de chats
    # TODO investigar como almacenar la lista de chats en current_user
    chats_url = f'http://backend-rest:8080/Service/u/{userid}/chats'
    r_chats = requests.get(chats_url)
    chatlist_json = r_chats.json() if r_chats.ok else []
    print('hecha query a get all chats')

    # Buscar el chat concreto
    chat_url = f'http://backend-rest:8080/Service/u/{userid}/chat/{chatid}'
    r_chat = requests.get(chat_url)
    print('printing la respuesta del chat especifico')
    r_chat.raise_for_status()
    print(r_chat.headers)
    print(r_chat.content)
    chat_seleccionado = r_chat.json() if r_chat.ok else None

    logging.info(chat_seleccionado)

    if not chat_seleccionado:
    # si el chat devuelto es null, muestra un error
        logging.error(f"Chat con ID {chatid} no encontrado para el usuario {userid}.")
        flash("Chat no encontrado.", "danger")
        return redirect(url_for('chats', userid=userid))

    if not chat_seleccionado.get('conversation'):
        chat_seleccionado['conversation'] = [
            {
                'prompt': '', 
                'answer': 'Hola, soy Llama. Escríbeme y dime en qué te puedo ayudar hoy.'
            }
        ]

    # Si todo va bien, renderiza el template chats.html
    return render_template('chats.html', userid=userid,
        chats=chatlist_json, chat_seleccionado=chat_seleccionado
    )
    
@app.route('/next', methods=['POST'])
@login_required
def next():
    
    dialogueid = request.form.get('dialogueid')
    user_prompt = request.form.get('prompt')
    userid = current_user.id

    next_token = request.form.get('next_token', '').strip()

    if not dialogueid or not user_prompt:
        flash("Error: No se ha seleccionado un chat o el mensaje está vacío.")
        print("No esta recibiendo el prompt")
        return redirect(url_for('chats'))

    datos = {
        "prompt": user_prompt
    }
    print("Entra a la peticion")

    query_url = f'http://backend-rest:8080/Service/u/{userid}/dialogue/{dialogueid}/next/{next_token}'
    request_id = None
    
    try:
        r = requests.post(query_url, json=datos, allow_redirects=False)
        print("DEBUG: Entra al try del next")
        if r.status_code in [201, 202]:    # ACCEPTED
            print("DEBUG: Peticion aceptada")
            location_url = r.headers.get('Location')
            if location_url and 't=' in location_url:
                request_id = location_url.split('t=')[-1].replace(']', '').replace('[', '').strip()

                print("DEBUG: Ticket capturado con éxito -> {request_id}")

        elif r.status_code == 204:  # BUSY
            flash("La IA está ocupada procesando otro mensaje. Espera un momento.")
        elif r.status_code == 400:  # FORMATO INVALIDO
            flash("Error: Formato de mensaje inválido.")

    except Exception as e:
        print(f"Error en el flujo: {e}")
        return redirect(url_for('chats'))
 
    r_chats = requests.get(f'http://backend-rest:8080/Service/u/{userid}/chats')
    lista_chats = r_chats.json() if r_chats.ok else []

    r_chat = requests.get(f'http://backend-rest:8080/Service/u/{userid}/chat/{dialogueid}')
    chat_actual = r_chat.json() if r_chat.ok else None

    return render_template('chats.html', 
                               chats=lista_chats,
                               chat_seleccionado=chat_actual,
                               userid=userid,
                               request_id=request_id)
    
@app.route('/consultar_estado/<dialogueid>/<request_id>')
@login_required
def consultar_estado(dialogueid, request_id):
    userid = current_user.id
    print("Empieza a consultar el estado")
    token_limpio = request_id.replace(']', '').replace('[', '').strip()
    print(request_id)
    query_url = f'http://backend-rest:8080/Service/u/{userid}/dialogue/{dialogueid}?t={token_limpio}'
    
    try:
        r = requests.get(query_url)
        
        if r.status_code == 202:
             return jsonify({"status": "BUSY"}), 200
            
        elif r.status_code == 200:
            chat_dto = r.json() 
            
            conversations = chat_dto.get('conversation', [])
            
            if conversations:
                ultima_conv = conversations[-1]
                respuesta_texto = ultima_conv.get('answer', '')
            else:
                respuesta_texto = "Error: El historial de mensajes está vacío."

            return jsonify({
                "status": "READY",
                "answer": respuesta_texto,
                "next_token": chat_dto.get('nextUrl') # El nuevo token generado en Java
            }), 200

        elif r.status_code == 404:
            return jsonify({"status": "ERROR", "message": "Chat no encontrado"}), 404
        else:
            return jsonify({"status": "ERROR", "message": "Fallo en el servidor Llama"}), 500

    except Exception as e:
        print(f"DEBUG: Error en polling de ticket {request_id}: {e}")
        return jsonify({"status": "ERROR", "message": "Error de conexión"}), 500

@app.route('/chats/u/nuevochat', methods=['GET','POST'])
@login_required
def nuevochat():
    print("entrando en nuevochat")
    chat_name = request.form.get('chat_name')
    # logging.info(chat_name)
    # TODO: cambiar esto según el nombre del endpoint
    userid = current_user.id
    query_url = f'http://backend-rest:8080/Service/u/{userid}/nuevochat'
    chat_data = chat_name
    r = requests.post(query_url, chat_data)

    # que me devuvea el created
    if r.status_code == 200:
        # request para actualizar la lista de chats
        chats_url = f'http://backend-rest:8080/Service/u/{userid}/chats'
        print('Empieza el print')
        try:
            r_chats = requests.get(chats_url)
            r_chats.raise_for_status()
            chatlist_json = r_chats.json()
            print(chatlist_json)

        except requests.RequestException as e:
            logging.error(f"Error al obtener los chats: {e}")
            chatlist_json = []

        #Busca el chat en la lista
        chat_seleccionado = None
        if chatlist_json:
            for chat in chatlist_json:
                print(chat['name'])
                if chat['name'] == chat_name:
                    chat_seleccionado = chat    
                    break

        return render_template('chats.html', userid=current_user.id, chat_seleccionado=chat_seleccionado, chats=chatlist_json)
    else:
        flash("Error al crear el chat. Inténtalo de nuevo.", "danger")
        return redirect(url_for('chats'))

@app.route('/profile')
@login_required
def profile():
    if not current_user.is_authenticated:
        return render_template('index.html')

    form = ProfileUpdateForm()
    return render_template('profile.html', form=form)

@app.route('/u/<userid>/profile/cambiardatos', methods=['POST'])
@login_required
def cambiar_datos(userid):
    ##  TODO si no actualiza correctamente el user recibe 406 NOT_ACCEPTABLE
    if not current_user.is_authenticated:
        return render_template('index.html')
    else:
        print("hola")
        error = None
        form = ProfileUpdateForm()
        print(form.newmail, form.newname)
        print("request.method", request.method, form.validate_on_submit())
        if request.method == "POST":
            userid = current_user.id    
            query_url = f'http://backend-rest:8080/Service/profile/{userid}/cambiardatos'
            userdata = {
                'name' : form.newname.data,
                'email' : form.newmail.data
            }
            headers = {'Content-Type': 'application/json'}
            print("viene userdata")
            print(userdata)

            r = requests.post(query_url, json=userdata, headers=headers)

            print(r.status_code)
            print(r.text)
            if r.ok:
                # recibir los datos del usuario desde el backend y construir un usuario
                # buscarlo en users (load_user) y si no está, añadirlo
                # llamar a login_user
                current_user.name = form.newname
                current_user.mail = form.newmail
                print("nombre cambiado todo guay")

            return render_template('profile.html', form=form) 
        else: 
            print("eres tontito")
        return redirect(url_for('profile'))

@app.route('/profile/cambiar_password', methods=['POST'])
@login_required
def cambiar_password():
    ## TODO si no actualiza correctamente el user recibe 406 NOT_ACCEPTABLE
    if not current_user.is_authenticated:
        return render_template('index.html')
    else:
        error = None
        if request.method == "POST":
            form = PasswordUpdateForm(None if request.method != 'POST' else request.form)
            query_url = ('http://backend-rest:8080/Service/u/{userid}/profile/cambiar_password')
            userdata = {
                'oldPassword' : form.oldpass.data,
                'newPassword' : form.newpass.data
            }
            headers = {'Content-Type': 'application/json'}

            r = requests.post(query_url, json=userdata, headers=headers)

            if r.ok:
                json_user = r.json()
                print("contraseña cambiada todo guay")

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 5010)))

