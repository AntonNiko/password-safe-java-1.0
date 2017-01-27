import sqlite3

class Database():
    def __init__(self,name):
        self.conn = sqlite3.connect("{}.db".format(name))
        self.createUserTable()

    def createUserTable(self):
        self.conn.execute(""" CREATE TABLE collection
                  (ID INT PRIMARY KEY NOT NULL,
                  NAME TEXT NOT NULL,
                  PASSWORD CHAR(200))
        """)
        
if __name__ == '__main__':
    d = Database("users")
