import re

from pydantic import BaseModel, Field, field_validator


class Registration(BaseModel):
    email: str = Field(min_length=3, max_length=320)
    name: str = Field(min_length=1, max_length=120)
    password: str = Field(min_length=8, max_length=72)

    @field_validator("email")
    @classmethod
    def valid_email(cls, value):
        value = value.strip().lower()
        if not re.fullmatch(r"[^\s@]+@[^\s@]+\.[^\s@]+", value):
            raise ValueError("Correo inválido")
        return value

    @field_validator("name")
    @classmethod
    def clean_name(cls, value):
        value = value.strip()
        if not value:
            raise ValueError("El nombre es obligatorio")
        return value


class Credentials(BaseModel):
    email: str
    password: str

    @field_validator("email")
    @classmethod
    def normalize_email(cls, value):
        return value.strip().lower()


class RatingInput(BaseModel):
    movie_id: int
    rating: float = Field(ge=0.5, le=5.0)


class TagInput(BaseModel):
    tag: str = Field(min_length=1, max_length=100)

    @field_validator("tag")
    @classmethod
    def clean_tag(cls, value):
        value = value.strip()
        if not value:
            raise ValueError("Tag vacío")
        return value
